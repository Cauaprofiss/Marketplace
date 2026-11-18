(() => {
  const showToast = (message, type = 'success') => CampusFeedback.show(message, type);
  document.querySelectorAll('[data-profile-favorite]').forEach(button => button.addEventListener('click', async () => {
    if (button.disabled) return; button.disabled = true;
    try {
      const response = await fetch(`/favoritos/${button.dataset.productId}/alternar`, { method: 'POST' });
      if (response.status === 401) { window.location.href = '/login'; return; }
      if (!response.ok) throw new Error('Não foi possível atualizar os favoritos.');
      const result = await response.json(); button.classList.toggle('saved', result.favorito); button.setAttribute('aria-pressed', String(result.favorito)); button.setAttribute('aria-label', result.favorito ? 'Remover dos favoritos' : 'Adicionar aos favoritos'); showToast(result.favorito ? 'Produto adicionado aos favoritos.' : 'Produto removido dos favoritos.');
    } catch (error) { showToast(error.message, 'error'); } finally { button.disabled = false; }
  }));
})();
