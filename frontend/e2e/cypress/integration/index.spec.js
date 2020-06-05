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


});
