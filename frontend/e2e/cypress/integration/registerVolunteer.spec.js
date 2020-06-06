describe('Volunteer should', () => {

  it('open the web', () => {
    cy.visit('/');
  });

  it('visit the registration page', () => {
    cy.visit('/volunteer-register');
    cy.get('form');
    cy.get('h1').should('contain', 'Registro');
  });

  it('visit the login page', () => {
    cy.visit('/volunteer-login');
    cy.get('form');
    cy.get('h1').should('contain', 'Acceso');
  });

  it('verify resend to email confirmation page when the form is good way', () => {
    const {email, password} = {email: 'test@test.com', password: 'aLongPassword'};

    cy.visit('/volunteer-register');

    cy.get('input[name=email]').type(`${email}`);
    cy.get('input[name=password]').type(`${password}`);
    cy.get('input[name=repeatedPassword]').type(`${password}`);
    cy.get('button[type=submit]').click();

    cy.url().should('include', '/email-confirmation-register');
    cy.get('p').should('contain', 'reenviar correo')
  });


});
