describe('Volunteer should', () => {

  it('open the web', () => {
    cy.visit('/');
  });

  it('verify resend to email confirmation page when the form is good way', () => {
    const randomEmail = (Math.random() * 1000).toString();
    const {email, password} = {email: `test${randomEmail}@test.com`, password: 'aLongPassword'};

    cy.visit('/');
    cy.get('button[aria-label=register-button]').click();
    cy.get('input[name=email]').type(`${email}`);
    cy.get('input[name=password]').type(`${password}`);
    cy.get('input[name=repeatedPassword]').type(`${password}`);
    cy.get('button[type=submit]').click();

    cy.url().should('include', '/email-confirmation-register');
    cy.get('p').should('contain', 'reenviar correo')
  });


});
