(() => {
  document.querySelectorAll('[data-character-limit]').forEach(field => {
    const counter = document.querySelector(`[data-character-counter-for="${field.id}"]`);
    const remainingText = counter?.querySelector('[data-remaining]');

    if (!counter || !remainingText || field.maxLength < 0) return;

    const updateCounter = () => {
      const remaining = Math.max(0, field.maxLength - field.value.length);
      remainingText.textContent = String(remaining);
      counter.classList.toggle('is-near-limit', remaining <= Math.ceil(field.maxLength * 0.1));
    };

    field.addEventListener('input', updateCounter);
    updateCounter();
  });
})();
