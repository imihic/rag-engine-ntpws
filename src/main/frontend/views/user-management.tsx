import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Grid, GridColumn, Button, Notification } from '@vaadin/react-components';
import { useState, useEffect } from 'react';
import { UserEndpoint } from 'Frontend/generated/endpoints';
import User from 'Frontend/generated/hr/combis/application/data/model/User'; // Import default export

export const config: ViewConfig = {
  menu: { order: 4, icon: 'line-awesome/svg/users-cog-solid.svg' },
  title: 'User Management',
  rolesAllowed: ['ADMIN'],
};

// Map Backend User to Frontend User
interface FrontendUser {
  id: string;
  username: string;
  name: string;
  enabled: boolean;
}

export default function UserManagementView() {
  const [users, setUsers] = useState<FrontendUser[]>([]);
  const [notificationOpen, setNotificationOpen] = useState<boolean>(false);
  const [notificationText, setNotificationText] = useState<string>('');

  useEffect(() => {
    async function fetchUsers() {
      try {
        const usersFromBackend: User[] = await UserEndpoint.getAllUsers();
        const mappedUsers: FrontendUser[] = usersFromBackend.map((user) => ({
          id: user.id?.toString() || '', // Convert `number | undefined` to `string`
          username: user.username || 'Unknown',
          name: user.name || 'Unnamed',
          enabled: !!user.enabled, // Ensure boolean type
        }));
        setUsers(mappedUsers);
      } catch (error) {
        console.error('Failed to fetch users:', error);
      }
    }
    fetchUsers();
  }, []);

  const toggleUserAccess = async (userId: string, enabled: boolean) => {
    try {
      await UserEndpoint.setUserAccess(userId, !enabled);
      setUsers((prevUsers) =>
        prevUsers.map((user) =>
          user.id === userId ? { ...user, enabled: !enabled } : user
        )
      );
      setNotificationText(`User ${!enabled ? 'enabled' : 'disabled'} successfully.`);
      setNotificationOpen(true);
    } catch (error) {
      console.error('Failed to update user access:', error);
      setNotificationText('Failed to update user access. Please try again.');
      setNotificationOpen(true);
    }
  };

  // @ts-ignore
  // @ts-ignore
  return (
    <div className="flex flex-col h-full p-l box-border">
      <Grid items={users} className="w-full">
        <GridColumn path="username" header="Username" />
        <GridColumn path="name" header="Name" />
        <GridColumn
          header="Status"
          renderer={({ item }: { item: FrontendUser }) => (
            <Button
              theme={item.enabled ? 'success' : 'error'}
              onClick={() => toggleUserAccess(item.id, item.enabled)}
            >
              {item.enabled ? 'Disable' : 'Enable'}
            </Button>
          )}
        />
      </Grid>
      <Notification
        duration={3000}
        position="bottom-center"
        open={() => notificationOpen} // Wrap `notificationOpen` in an arrow function
        onClosed={() => setNotificationOpen(false)}
      >
        {notificationText}
      </Notification>

    </div>
  );
}
