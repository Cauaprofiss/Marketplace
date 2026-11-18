(() => {
  const DISPLAY_MS = 3000;
  const FADE_MS = 500;
  const pending = new WeakMap();
  let toast;
  let container;

  // Fora dos cartões e áreas animadas, para fixar os avisos à janela.
  const getContainer = () => {
    if (!container) {
      container = document.createElement('div');
      container.className = 'campus-notices';
      document.body.appendChild(container);
    }
    return container;
  };

  const cancel = element => {
    const timers = pending.get(element);
    if (timers) timers.forEach(window.clearTimeout);
    pending.delete(element);
  };

  const hide = element => {
    if (!element) return;
    cancel(element);
    element.hidden = true;
    element.classList.remove('is-entering', 'is-leaving');
  };

  // A nova tentativa cancela inclusive a saída anterior do mesmo aviso.
  const showIn = (element, message, type = 'error') => {
    if (!element || !message?.trim()) return;
    cancel(element);
    element.textContent = message;
    element.classList.add('campus-notice', 'campus-notice--toast');
    getContainer().appendChild(element);
    element.classList.toggle('campus-notice--error', type === 'error');
    element.setAttribute('role', type === 'error' ? 'alert' : 'status');
    element.setAttribute('aria-live', type === 'error' ? 'assertive' : 'polite');
    element.setAttribute('aria-atomic', 'true');
    // Só libera a primeira exibição depois de entrar no canto da janela.
    element.setAttribute('data-campus-notice-ready', '');
    element.hidden = false;
    element.classList.remove('is-entering', 'is-leaving');
    // Reinicia a entrada mesmo quando o texto da nova tentativa é idêntico.
    void element.offsetWidth;
    element.classList.add('is-entering');
    const leave = window.setTimeout(() => {
      element.classList.remove('is-entering');
      element.classList.add('is-leaving');
      const finish = window.setTimeout(() => hide(element), FADE_MS);
      pending.set(element, [finish]);
    }, DISPLAY_MS);
    pending.set(element, [leave]);
    return element;
  };

  const show = (message, type = 'success') => {
    if (!toast) toast = document.querySelector('.campus-notice--toast[data-campus-notice]');
    if (!toast) {
      toast = document.createElement('div');
      toast.className = 'campus-notice campus-notice--toast';
      toast.hidden = true;
      getContainer().appendChild(toast);
    }
    return showIn(toast, message, type);
  };

  window.CampusFeedback = {
    show, showIn, hide,
    success: message => show(message, 'success'),
    error: message => show(message, 'error')
  };

  const init = () => {
    document.querySelectorAll('[data-campus-notice]').forEach(element => {
      showIn(element, element.textContent.trim(),
        element.classList.contains('campus-notice--error') ? 'error' : 'success');
    });
    // Mantém as restrições HTML e troca apenas o balão nativo pelo aviso padrão.
    // O evento ocorre novamente em cada tentativa, inclusive para o mesmo erro.
    document.addEventListener('invalid', event => {
      const field = event.target;
      if (!(field instanceof HTMLInputElement || field instanceof HTMLSelectElement ||
            field instanceof HTMLTextAreaElement)) return;
      event.preventDefault();
      const form = field.form;
      if (form && form.querySelector(':invalid') !== field) return;
      const label = field.labels?.[0]?.textContent.trim().replace(/[:*]\s*$/, '');
      const message = field.validity.valueMissing && label
        ? 'Preencha o campo ' + label + '.'
        : field.validationMessage;
      show(message, 'error');
      if (field.getClientRects().length) field.focus();
    }, true);
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();
