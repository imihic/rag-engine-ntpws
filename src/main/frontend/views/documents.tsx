import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Upload, Notification, Grid, GridColumn, Button, GridSortColumn } from '@vaadin/react-components';
import { useState, useEffect } from 'react';
import { UserEndpoint } from 'Frontend/generated/endpoints';
import { Icon } from '@vaadin/react-components/Icon';

export const config: ViewConfig = {
  menu: { order: 2, icon: 'line-awesome/svg/file-alt.svg' },
  title: 'Documents',
  loginRequired: true,
};

export default function DocumentsView() {
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [notificationText, setNotificationText] = useState('');
  const [userId, setUserId] = useState<string | null>(null);

  const [uploadedFiles, setUploadedFiles] = useState([
    { id: 1, name: 'Nietschze.pdf', uploadDate: '2024-11-20', size: '1.2 MB' },
    { id: 2, name: 'Nietschze 2.pdf', uploadDate: '2024-11-21', size: '0.8 MB' },
  ]);

  useEffect(() => {
    async function fetchUserId() {
      try {
        const id = await UserEndpoint.getAuthenticatedUserId();
        if (id !== undefined) {
          setUserId(id.toString()); // Convert number to string
        } else {
          console.error('User ID is undefined');
        }
      } catch (error) {
        console.error('Failed to fetch user ID:', error);
      }
    }
    fetchUserId();
  }, []);

  const handleUploadSuccess = (event: any) => {
    // Extract file details from the event
    const file = event.detail.file;
    const newId = uploadedFiles.length > 0 ? uploadedFiles[uploadedFiles.length - 1].id + 1 : 1;
    const newFile = {
      id: newId, // Increment ID based on the last file
      name: file.name,
      uploadDate: new Date().toISOString().split('T')[0], // Current date
      size: `${(file.size / 1048576).toFixed(2)} MB`, // Convert size to MB
    };

    // Append the new file to the list
    setUploadedFiles((prevFiles) => [...prevFiles, newFile]);

    // Show success notification
    setNotificationText('File uploaded successfully!');
    setNotificationOpen(true);
  };
  const handleUploadError = () => {
    setNotificationText('File upload failed. Please try again.');
    setNotificationOpen(true);
  };

  const handleDelete = (fileId: number) => {
    setUploadedFiles((prevFiles) => prevFiles.filter((file) => file.id !== fileId));
    setNotificationText('File deleted successfully!');
    setNotificationOpen(true);
  };

  return (
    <div className="flex flex-col h-full p-l text-center box-border">
      {userId ? (
        <Upload
          maxFiles={1}
          maxFileSize={10485760}
          target="/api/v1/documents/upload"
          headers={{ user_id: userId }} // Set the dynamic user ID here
          className="w-full"
          onUploadSuccess={handleUploadSuccess}
          onUploadError={handleUploadError}
        />
      ) : (
        <div>Loading...</div> // Show a loading state until the user ID is fetched
      )}
      {/* Notification */}
      <Notification
        duration={3000}
        position="bottom-center"
        open={() => notificationOpen} // Pass a function returning the boolean state
        onClosed={() => setNotificationOpen(false)}
      >
        {notificationText}
      </Notification>
      {/* Uploaded Files Grid */}
      <Grid items={uploadedFiles} style={{ marginTop: '20px', width: '100%' }}>
        <GridSortColumn path="name" header="File Name" />
        <GridSortColumn path="uploadDate" header="Upload Date" />
        <GridSortColumn path="size" header="Size" />
        <GridColumn
          header="Actions"
          renderer={({ item }) => (
            <Button
              theme="icon error"
              aria-label="Delete"
              onClick={() => handleDelete(item.id)}
            >
              <Icon icon="vaadin:trash" />
            </Button>
          )}
        />
        <span slot="empty-state">No documents found.</span>
      </Grid>
    </div>
  );
}
