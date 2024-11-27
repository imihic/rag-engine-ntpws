import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import React, { useEffect, useState } from 'react';
import { UserEndpoint } from 'Frontend/generated/endpoints';
import UserSettingsDto from 'Frontend/generated/hr/combis/application/api/dto/UserSettingsDto';
import { Button } from '@vaadin/react-components/Button';
import { Checkbox } from '@vaadin/react-components/Checkbox';
import { ComboBox } from '@vaadin/react-components/ComboBox';
import { FormItem } from '@vaadin/react-components/FormItem';
import { FormLayout } from '@vaadin/react-components/FormLayout';
import { Notification } from '@vaadin/react-components/Notification';
import { NumberField } from '@vaadin/react-components/NumberField';
import { VerticalLayout } from '@vaadin/react-components';
import { HorizontalLayout } from '@vaadin/react-components/HorizontalLayout';




export const config: ViewConfig = {
  menu: { order: 3, icon: 'line-awesome/svg/cog-solid.svg' },
  title: 'Settings',
  loginRequired: true,
};

export default function SettingsView() {
  const [settings, setSettings] = useState<UserSettingsDto | null>(null);

  const [notificationOpened, setNotificationOpened] = useState(false);
  const [notificationText, setNotificationText] = useState('');

  useEffect(() => {
    // Fetch user settings
    UserEndpoint.getUserSettings().then((fetchedSettings) => {
      setSettings(fetchedSettings);

      // Apply theme based on settings
      applyTheme(fetchedSettings.darkModeEnabled ?? false);


    });
  }, []);

  const applyTheme = (darkModeEnabled: boolean) => {
    const root = document.documentElement;
    // change body theme attribute
    const body = document.body;
    if (darkModeEnabled) {
      root.setAttribute('theme', 'dark');
      body.setAttribute('theme', 'dark');
    } else {
      root.setAttribute('theme', 'light');
      body.setAttribute('theme', 'light');
    }
  };

  const handleSave = () => {
    if (settings) {
      UserEndpoint.updateUserSettings(settings).then(() => {
        setNotificationText('Settings saved successfully');
        setNotificationOpened(true);
        // Apply theme immediately
        if (settings.darkModeEnabled) {
          document.documentElement.setAttribute('theme', 'dark');
        } else {
          document.documentElement.removeAttribute('theme');
        }


      });
    }
  };

  const responsiveSteps = [
    { minWidth: '0px', columns: 1 },
  ];

  return (
    <VerticalLayout className="p-l h-l m-m" >
      {settings && (
        <FormLayout responsiveSteps={responsiveSteps} >
          <h3>Assistant Settings</h3>

          <FormItem>
            <label slot="label">Model</label>
            <ComboBox
              items={['gpt-3.5-turbo', 'gpt-4']}
              value={settings.openAiModel || 'gpt-3.5-turbo'}
              onValueChanged={(e) =>
                setSettings({ ...settings, openAiModel: e.detail.value })
              }
              style={{ width: '50%' }}
            />
          </FormItem>

          <FormItem>
            <label slot="label">Temperature</label>
            <NumberField
              min={0}
              max={1}
              step={0.1}
              value={(settings.temperature || 0.7).toString()}
              onValueChanged={(e) =>
                setSettings({
                  ...settings,
                  temperature: parseFloat(e.detail.value),
                })
              }
              style={{ width: '50%' }}
            />
          </FormItem>

          {/* Accessibility Settings */}
          <h3>Accessibility Settings</h3>

          <FormItem>
            <label slot="label">Dark Mode</label>
            <Checkbox
              checked={settings.darkModeEnabled || false}
              onCheckedChanged={(e) => {
                const updatedSettings = { ...settings, darkModeEnabled: e.detail.value };
                setSettings(updatedSettings);
                applyTheme(updatedSettings.darkModeEnabled);
              }}
            />
          </FormItem>


          <HorizontalLayout className="mt-m">
            <Button theme="primary" onClick={handleSave}>
              Save Settings
            </Button>
          </HorizontalLayout>
        </FormLayout>
      )}

      {notificationOpened && (
        <Notification
          opened={notificationOpened}
          onOpenedChanged={(e) => setNotificationOpened(e.detail.value)}
          position="top-stretch"
        >
          {notificationText}
        </Notification>
      )}
    </VerticalLayout>
  );
}
