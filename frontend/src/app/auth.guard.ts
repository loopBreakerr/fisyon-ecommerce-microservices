import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import Keycloak from 'keycloak-js';

export const authGuard: CanActivateFn = () => {
  const keycloak = inject(Keycloak);

  if (!keycloak.authenticated) {
    keycloak.login();
    return false;
  }

  return true;
};
