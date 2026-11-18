(() => {
  const form = document.getElementById('checkoutForm');
  if (!form) return;
  const button = document.getElementById('continueBtn');
  const date = document.getElementById('dataRetirada');
  const time = document.getElementById('horarioRetirada');
  const updatePayment = () => {
    const selected = form.querySelector('input[name="formaPagamento"]:checked');
    button.textContent = selected?.value === 'pix' ? 'Continuar com Pix' : 'Continuar com retirada';
  };
  const validatePickup = () => {
    let message = '';
    if (date.value && time.value && new Date(date.value + 'T' + time.value) <= new Date()) {
      message = 'Escolha uma data e um horário que ainda não passaram.';
    }
    date.setCustomValidity(message);

    date.setAttribute('aria-invalid', message ? 'true' : 'false');
    if (message) CampusFeedback.error(message);
  };
  form.querySelectorAll('input[name="formaPagamento"]').forEach(input => input.addEventListener('change', updatePayment));
  date.addEventListener('input', validatePickup);
  time.addEventListener('change', validatePickup);
  form.addEventListener('submit', event => {
    validatePickup();
    if (!form.reportValidity()) event.preventDefault();
  });
  updatePayment();
})();
