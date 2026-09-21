import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import Keycloak from 'keycloak-js';

export const customerGuard: CanActivateFn = () => {
  const keycloak = inject(Keycloak);
  const router = inject(Router);

  if (!keycloak.authenticated) {
    keycloak.login();
    return false;
  }

  const roles = keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
  if (roles.includes('seller') || roles.includes('admin')) {
    router.navigate(['/products']);
    return false;
  }

  return true;
};
