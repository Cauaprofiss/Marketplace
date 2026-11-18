(() => {
  const grid = document.querySelector('[data-favorites-grid]');
  const empty = document.querySelector('[data-favorites-empty]');
  const count = document.querySelector('[data-favorites-count]');
  if (!grid || !empty || !count) return;

  const updateState = () => {
    const total = grid.querySelectorAll('[data-favorite-card]').length;
    count.textContent = `${total} ${total === 1 ? 'produto' : 'produtos'}`;
    empty.hidden = total > 0;
    grid.classList.toggle('is-empty', total === 0);
  };

  grid.addEventListener('click', async event => {
    const button = event.target.closest('.favorite-remove');
    if (!button || button.disabled) return;
    event.preventDefault();
    event.stopPropagation();

    button.disabled = true;
    try {
      const response = await fetch(`/favoritos/${button.dataset.productId}/alternar`, { method: 'POST' });
      if (response.status === 401) {
        window.location.href = '/login';
        return;
      }
      if (!response.ok) throw new Error('Não foi possível remover o favorito.');

      const result = await response.json();
      if (result.favorito) {
        button.disabled = false;
        return;
      }

      const card = button.closest('[data-favorite-card]');
      card.classList.add('is-removing');
      window.setTimeout(() => {
        card.remove();
        updateState();
      }, 220);
      CampusFeedback.success('Produto removido dos favoritos.');
    } catch (error) {
      button.disabled = false;
      CampusFeedback.error(error.message);
    }
  });

  updateState();
})();
