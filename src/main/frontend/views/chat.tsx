import React, { useEffect } from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { useSignal } from '@vaadin/hilla-react-signals';
import {
    Button,
    HorizontalLayout,
    Icon,
    MessageInput,
    MessageList,
    MessageListItem,
    Tooltip,
    VerticalLayout,
    VirtualList,
} from '@vaadin/react-components';
import { AiService, ChatEndpoint } from 'Frontend/generated/endpoints.js';
import Message from 'Frontend/generated/hr/combis/application/data/model/Message';

export const config: ViewConfig = {
    menu: { order: 1, icon: 'line-awesome/svg/comments-solid.svg' },
    title: 'Chat',
    loginRequired: true,
};

interface FrontendChat {
    id: number;
    firstMessage: string;
    startTime: Date;
    messages: MessageListItem[];
}

function truncateMessage(message: string, maxLength: number): string {
    return message.length > maxLength ? message.slice(0, maxLength) + '...' : message;
}

function formatStartTime(date: Date): string {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric',
    }).format(date);
}

export default function ChatView() {
    const messages = useSignal<MessageListItem[]>([]);
    const pastChats = useSignal<FrontendChat[]>([]);
    const activeChat = useSignal<FrontendChat | null>(null);
    const temporaryChatIdRef = React.useRef(-1);

    useEffect(() => {
        async function fetchChats() {
            const chatsFromBackend = await ChatEndpoint.getUserChats();
            pastChats.value = chatsFromBackend.map((chat) => {
                const messages = chat.messages?.filter((message): message is Message => message != null) ?? [];
                return {
                    id: chat.id ?? 0,
                    firstMessage: truncateMessage(messages[0]?.content ?? '', 30),
                    startTime: chat.createdAt ? new Date(chat.createdAt) : new Date(),
                    messages: messages.map((message) => ({
                        userName: message.sender === 'USER' ? 'You' : 'Assistant',
                        text: message.content ?? '',
                    })),
                };
            });
        }
        fetchChats();
    }, []);

    function deleteChat(chatId: number) {
        pastChats.value = pastChats.value.filter((chat) => chat.id !== chatId);
        if (activeChat.value?.id === chatId) {
            activeChat.value = null;
            messages.value = [];
        }
    }

    function createNewChat() {
        const newChat: FrontendChat = {
            id: temporaryChatIdRef.current--,
            firstMessage: 'New chat',
            startTime: new Date(),
            messages: [],
        };
        activeChat.value = newChat;
        pastChats.value = [...pastChats.value, newChat];
        messages.value = [];
    }

    async function sendMessage(messageContent: string) {
        if (!activeChat.value) {
            console.error('No active chat');
            return;
        }

        if (activeChat.value.id < 0) {
            // The chat is not yet saved in the backend
            const chatId = await AiService.createChat();
            if (chatId === undefined) {
                throw new Error('Failed to create chat: chatId is undefined');
            }
            const oldId = activeChat.value.id;
            activeChat.value.id = chatId;
            pastChats.value = pastChats.value.map((chat) =>
              chat.id === oldId ? activeChat.value! : chat
            );
        }

        // Update the firstMessage if this is the first message sent
        if (activeChat.value.messages.length === 0) {
            activeChat.value.firstMessage = truncateMessage(messageContent, 30);
            pastChats.value = pastChats.value.map((chat) =>
              chat.id === activeChat.value!.id ? activeChat.value! : chat
            );
        }

        // Send the user's message
        const userMessage: MessageListItem = { userName: 'You', text: messageContent };
        messages.value = [...messages.value, userMessage];
        activeChat.value.messages = [...activeChat.value.messages, userMessage];
        await ChatEndpoint.saveMessage(activeChat.value.id, 'USER', messageContent);

        // Handle the assistant's response (streaming)
        // Handle the assistant's response (streaming)
        let firstChunk = true;

        AiService.chatStream(activeChat.value.id, messageContent)
          .onNext((chunk) => {
              if (chunk) {
                  if (firstChunk) {
                      firstChunk = false;

                      const assistantMessage: MessageListItem = { userName: 'Assistant', text: chunk };
                      messages.value = [...messages.value, assistantMessage];
                      if (activeChat.value) {
                          activeChat.value.messages = [...activeChat.value.messages, assistantMessage];
                      }
                  } else {
                      // Update the last assistant message
                      messages.value = messages.value.map((msg, index) =>
                        index === messages.value.length - 1
                          ? { ...msg, text: msg.text + chunk }
                          : msg
                      );

                      if (activeChat.value) {
                          const currentChat = activeChat.value; // Store in a local variable for clarity
                          currentChat.messages = currentChat.messages.map((msg, index) =>
                            index === currentChat.messages.length - 1
                              ? { ...msg, text: msg.text + chunk }
                              : msg
                          );
                      }
                  }
              }
          })
          .onError(() => {
              console.error('Error during chatStream');
          });

    }

    async function selectChat(chat: FrontendChat) {
        activeChat.value = chat;
        const messagesFromBackend = await ChatEndpoint.getChatMessages(chat.id ?? 0);
        messages.value = messagesFromBackend
          .filter((backendMessage): backendMessage is Message => backendMessage != null)
          .map((backendMessage) => ({
              userName: backendMessage.sender === 'USER' ? 'You' : 'Assistant',
              text: backendMessage.content ?? '',
          }));
    }

    // Renderer function for VirtualList
    const chatItemRenderer = ({ item: chat }: { item: FrontendChat }) => (
      <div
        key={chat.id}
        onClick={() => selectChat(chat)}
        className={`chat-item ${activeChat.value?.id === chat.id ? 'active' : ''}`}
        style={{ position: 'relative' }}>
          <HorizontalLayout>
              <div className="chat-item-header flex-grow">
                  <VerticalLayout>
                      <b>{chat.firstMessage}</b>
                      <span className="chat-item-timestamp">{formatStartTime(chat.startTime)}</span>
                  </VerticalLayout>
              </div>
              <div>
                  <Button
                    theme="small icon tertiary"
                    aria-label="Close"
                    onClick={(e) => {
                        e.stopPropagation(); // Prevent triggering selectChat
                        deleteChat(chat.id);
                    }}
                    style={{
                        cursor: 'pointer',
                        alignSelf: 'right',
                    }}>
                      <Icon icon="vaadin:close-small" />
                      <Tooltip slot="tooltip" text="Delete this chat" />
                  </Button>
              </div>
          </HorizontalLayout>
      </div>
    );

    return (
      <HorizontalLayout theme="spacing" style={{ alignItems: 'stretch', flexGrow: 1, height: '93vh' }}>
          {/* Left Side: Active Chat */}
          <div className="p-m flex flex-col box-border" style={{ flexGrow: 3, flexBasis: '75%', display: 'flex' }}>
              <MessageList items={messages.value} style={{ flexGrow: 1 }} />
              <MessageInput onSubmit={(e) => sendMessage(e.detail.value)} />
          </div>

          {/* Right Side: Past Chats */}
          <div
            className="padding flex flex-col box-border virtual-list"
            style={{ flexGrow: 1, flexBasis: '25%', display: 'flex' }}>
              <div className="content-stretch">
                  <Button theme="inline tertiary" onClick={createNewChat}>
                      New chat
                  </Button>
              </div>
              <VirtualList items={pastChats.value} style={{ flexGrow: 1 }}>
                  {chatItemRenderer}
              </VirtualList>
          </div>
      </HorizontalLayout>
    );
}
