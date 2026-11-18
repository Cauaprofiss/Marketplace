(() => {
  const unavailablePopup = document.querySelector('[data-unavailable-popup]');
  if (unavailablePopup) {
    const dismiss = () => {
      unavailablePopup.remove();
      document.body.classList.remove('unavailable-popup-open');
    };
    const dismissButton = unavailablePopup.querySelector('[data-unavailable-close]');
    document.body.classList.add('unavailable-popup-open');
    dismissButton?.addEventListener('click', dismiss);
    unavailablePopup.addEventListener('click', event => {
      if (event.target === unavailablePopup) dismiss();
    });
    document.addEventListener('keydown', event => {
      if (event.key === 'Escape') dismiss();
    });
    dismissButton?.focus();
  }

  const productTitle = document.querySelector('[data-product-title]');
  const productTitleText = productTitle?.querySelector('[data-product-title-text]');
  const productTitleButton = productTitle?.querySelector('[data-product-title-toggle]');
  const fullProductTitle = productTitleText?.textContent.trim() || '';
  const configureExpandableTitle = () => {
    if (!productTitle || productTitle.classList.contains('title-expanded')) return;
    productTitle.classList.remove('title-truncated');
    productTitleText.textContent = fullProductTitle;
    const truncated = productTitleText.scrollWidth > productTitleText.clientWidth + 1;
    productTitle.classList.toggle('title-truncated', truncated);
    if (truncated) {
      const words = fullProductTitle.split(/\s+/);
      while (words.length > 1 && productTitleText.scrollWidth > productTitleText.clientWidth + 1) {
        words.pop();
        productTitleText.textContent = words.join(' ');
      }
      productTitleButton.setAttribute('aria-expanded', 'false');
      productTitleButton.setAttribute('aria-label', 'Mostrar título completo');
    } else {
      productTitleButton.setAttribute('aria-expanded', 'false');
    }
  };
  const toggleProductTitle = () => {
    if (!productTitle?.classList.contains('title-truncated')) return;
    const expanded = productTitle.classList.toggle('title-expanded');
    if (expanded) productTitleText.textContent = fullProductTitle;
    else configureExpandableTitle();
    productTitleButton.setAttribute('aria-expanded', String(expanded));
    productTitleButton.setAttribute('aria-label', expanded ? 'Recolher título' : 'Mostrar título completo');
  };
  productTitle?.addEventListener('click', toggleProductTitle);
  configureExpandableTitle();
  document.fonts?.ready.then(configureExpandableTitle);
  window.addEventListener('resize', configureExpandableTitle);

  const mainImage = document.querySelector('[data-main-image]');
  const thumbnails = [...document.querySelectorAll('[data-gallery-src]')];
  thumbnails.forEach(thumbnail => thumbnail.addEventListener('click', () => {
    if (!mainImage || thumbnail.classList.contains('active')) return;
    mainImage.classList.add('switching');
    window.setTimeout(() => { mainImage.src = thumbnail.dataset.gallerySrc; mainImage.classList.remove('switching'); }, 150);
    thumbnails.forEach(item => item.classList.remove('active'));
    thumbnail.classList.add('active');
  }));

  const quantity = document.querySelector('[data-quantity]');
  const clampQuantity = value => Math.min(Number(quantity?.max || Number.MAX_SAFE_INTEGER), Math.max(Number(quantity?.min || 1), Number(value) || 1));
  const cartForm = document.querySelector('#cartForm');
  cartForm?.addEventListener('submit', () => { quantity.value = clampQuantity(quantity.value); const button = cartForm.querySelector('.add-cart'); if (button) { button.disabled = true; button.querySelector('span').textContent = 'Adicionando...'; } });

  const showToast = (message, type = 'success') => CampusFeedback.show(message, type);
  const favoriteButton = document.querySelector('[data-product-favorite]');
  favoriteButton?.addEventListener('click', async () => {
    if (favoriteButton.disabled) return;
    favoriteButton.disabled = true;
    try {
      const response = await fetch(`/favoritos/${favoriteButton.dataset.productId}/alternar`, { method: 'POST' });
      if (response.status === 401) {
        window.location.href = '/login';
        return;
      }
      if (!response.ok) throw new Error('Não foi possível atualizar os favoritos.');
      const result = await response.json();
      favoriteButton.classList.toggle('saved', result.favorito);
      favoriteButton.setAttribute('aria-pressed', String(result.favorito));
      favoriteButton.setAttribute('aria-label', result.favorito ? 'Remover dos favoritos' : 'Adicionar aos favoritos');
      showToast(result.favorito ? 'Produto adicionado aos favoritos.' : 'Produto removido dos favoritos.');
    } catch (error) {
      showToast(error.message, 'error');
    } finally {
      favoriteButton.disabled = false;
    }
  });
  document.querySelectorAll('.header-search nav a').forEach(link => link.classList.toggle('active', link.textContent.trim() === 'Vitrine virtual'));
})();
