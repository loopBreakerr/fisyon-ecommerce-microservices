import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import { MessageService, ConfirmationService } from 'primeng/api';
import Aura from '@primeuix/themes/aura';
import { definePreset } from '@primeuix/themes';

const FisyonPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#e3f2fd',
      100: '#bbdefb',
      200: '#90caf9',
      300: '#64b5f6',
      400: '#42a5f5',
      500: '#1976d2',
      600: '#1565c0',
      700: '#0d47a1',
      800: '#0b3d91',
      900: '#082a66',
      950: '#051b40'
    }
  }
});
import {
  provideKeycloak,
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  withAutoRefreshToken,
  AutoRefreshTokenService,
  UserActivityService
} from 'keycloak-angular';

import { routes } from './app.routes';

const gatewayUrlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(http:\/\/localhost:8000)(\/.*)?$/i,
  bearerPrefix: 'Bearer'
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideKeycloak({
      config: {
        url: 'http://localhost:8080',
        realm: 'ecommerce-realm',
        clientId: 'frontend-client'
      },
      initOptions: {
        onLoad: 'check-sso',
        redirectUri: window.location.origin,
        pkceMethod: 'S256'
      },
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 60000
        })
      ],
      providers: [AutoRefreshTokenService, UserActivityService]
    }),
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [gatewayUrlCondition]
    },
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),
    providePrimeNG({
      theme: {
        preset: FisyonPreset,
        options: {
          darkModeSelector: '.app-dark'
        }
      },
      license: 'eyJpZCI6IjE5M2M0NTI1LTlmMzYtNDI0ZC05NjllLTVlMjUzM2YyNDJiZiIsInByb2R1Y3QiOiJwcmltZXVpIiwidGllciI6ImNvbW11bml0eSIsInR5cGUiOiJkZXYiLCJpYXQiOjE3ODgyMzg0NjUsImV4cCI6MTgxOTc3NDQ2NX0.KB_bbIj13gtqX-nGYNvAIDczXfF0O-fsHRdIB6W8r9DHpPTvrQ8t3X2xnYvZDhzBV1w3vUBoPKiTIfb8G7dxCQ'
    }),
    MessageService,
    ConfirmationService
  ]
};
