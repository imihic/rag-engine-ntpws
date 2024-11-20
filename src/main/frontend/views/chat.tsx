import React from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { useSignal } from '@vaadin/hilla-react-signals';
import {
    HorizontalLayout,
    MessageInput,
    MessageList,
    MessageListItem,
    VirtualList,
} from '@vaadin/react-components';
import { AiService } from 'Frontend/generated/endpoints.js';

export const config: ViewConfig = {
    menu: { order: 1, icon: 'line-awesome/svg/comments-solid.svg' },
    title: 'Chat',
    loginRequired: true,
};

interface Chat {
    id: number; // Assuming chatId is a number
    name: string;
    messages: MessageListItem[];
}

export default function ChatView() {
    const messages = useSignal<MessageListItem[]>([]);
    const pastChats = useSignal<Chat[]>([]);
    const activeChat = useSignal<Chat | null>(null);

    async function sendMessage(messageContent: string) {
        // If there's no active chat, initialize one
        if (!activeChat.value) {
            const chatId = await AiService.createChat();
            if (chatId === undefined) {
                throw new Error('Failed to create chat: chatId is undefined');
            }
            const newChat: Chat = {
                id: chatId,
                name: `Chat ${pastChats.value.length + 1}`,
                messages: [],
            };
            activeChat.value = newChat;
            pastChats.value = [...pastChats.value, newChat];
        }

        // Send the user's message
        const userMessage: MessageListItem = { userName: 'You', text: messageContent };
        messages.value = [...messages.value, userMessage];
        activeChat.value.messages = [...activeChat.value.messages, userMessage];

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


    function selectChat(chat: Chat) {
        activeChat.value = chat;
        messages.value = chat.messages;
    }

    // Renderer function for VirtualList
    const chatItemRenderer = ({ item: chat }: { item: Chat }) => (
        <div
            key={chat.id}
            onClick={() => selectChat(chat)}
            style={{ padding: '8px', cursor: 'pointer', borderBottom: '1px solid #ccc' }}
        >
            <b>{chat.name}</b>
            <div>{chat.messages.length} messages</div>
        </div>
    );

    return (
        <HorizontalLayout
            theme="spacing padding"
            style={{ alignItems: 'stretch', flexGrow: 1, height: '93vh' }} 
        >
            {/* Left Side: Active Chat */}
            <div
                className="p-m flex flex-col box-border"
                style={{ flexGrow: 3, flexBasis: '75%', display: 'flex' }}
            >
                <MessageList items={messages.value} style={{ flexGrow: 1 }} />
                <MessageInput onSubmit={(e) => sendMessage(e.detail.value)} />
            </div>

            {/* Right Side: Past Chats */}
            <div
                className="p-m flex flex-col box-border"
                style={{ flexGrow: 1, flexBasis: '25%', display: 'flex' }}
            >
                <VirtualList items={pastChats.value} style={{ flexGrow: 1 }}>
                    {chatItemRenderer}
                </VirtualList>
            </div>
        </HorizontalLayout>
    );
}
