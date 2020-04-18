type typeRoute = { home: string; volunteer: { login: string; register: string } };

export const ROUTE: typeRoute = {
  home: '/',
  volunteer: {
    register: '/volunteer-register',
    login: '/volunteer-login',
  },
};
