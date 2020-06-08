import { ROUTE } from '../../../src/utils/routes';

describe('Volunteer should', () => {

  const VOLUNTEER = {
    email: {
      random: `test${(Math.random() * 1000).toString()}@test.com`,
      duplicate: `email.is.registered@test.com`
    },

    password: 'aLongPassword'
  };

  beforeEach(() => {
    cy.visit('/');
    cy.get('button[aria-label=register-button]').click();
    cy.get('input[name=email]').type(`${VOLUNTEER.email.duplicate}`);
    cy.get('input[name=password]').type(`${VOLUNTEER.password}`);
    cy.get('input[name=repeatedPassword]').type(`${VOLUNTEER.password}`);
    cy.get('button[type=submit]').click();
  });

  it('open the web', () => {
    cy.visit(ROUTE.home);
  });

  it('verify resend to email confirmation page when the form is good way', () => {
    cy.visit(ROUTE.home);
    cy.get('button[aria-label=register-button]').click();
    cy.get('input[name=email]').type(`${VOLUNTEER.email.random}`);
    cy.get('input[name=password]').type(`${VOLUNTEER.password}`);
    cy.get('input[name=repeatedPassword]').type(`${VOLUNTEER.password}`);
    cy.get('button[type=submit]').click();

    cy.url().should('include', '/email-confirmation-register');
    cy.get('p').should('contain', 'reenviar correo');
  });

  xit('tell user that email are register', () => {
    cy.visit(ROUTE.home);
    cy.get('button[aria-label=register-button]').click();
    cy.get('input[name=email]').type(`${VOLUNTEER.email.duplicate}`);
    cy.get('input[name=password]').type(`${VOLUNTEER.password}`);
    cy.get('input[name=repeatedPassword]').type(`${VOLUNTEER.password}`);
    cy.get('button[type=submit]').click();

    cy.url().should('include', '/');
    cy.get('p').should('contain', 'Este email ya existe');
  });


});
