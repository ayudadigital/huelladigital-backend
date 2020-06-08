type typeRoute = {
  home: string;
  email: { confirmation: string; resendConfirmation: string };
};

export const ROUTE: typeRoute = {
  home: '/',
  email: {
    confirmation: '/email-confirmation-register',
    resendConfirmation: '/resend-email-confirmation',
  },
};
