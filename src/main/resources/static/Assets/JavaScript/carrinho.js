(() => {
  document.querySelectorAll('[data-quantity-form]').forEach(form => {
    const input = form.querySelector('input[name="quantidade"]');
    form.querySelectorAll('[data-quantity-change]').forEach(button => button.addEventListener('click', () => {
      input.value = Math.max(0, Number(input.value || 0) + Number(button.dataset.quantityChange));
      form.submit();
    }));
    input.addEventListener('change', () => form.submit());
  });
  const modal = document.getElementById('detailsModal');
  const close = document.getElementById('closeDetailsModal');
  if (!modal || !close) return;
  const setOpen = open => { modal.classList.toggle('open', open); modal.setAttribute('aria-hidden', String(!open)); };
  document.querySelectorAll('.open-details-btn').forEach(button => button.addEventListener('click', () => {
    const data = button.dataset;
    document.getElementById('detailsNome').textContent = data.nome || '';
    document.getElementById('detailsDescricao').textContent = data.descricao || 'Sem descrição.';
    document.getElementById('detailsPreco').textContent = `R$ ${data.preco || '0,00'}`;
    const category = document.getElementById('detailsCategoria'); category.textContent = data.categoria || ''; category.hidden = !data.categoria;
    const imageBox = document.getElementById('detailsImg'); const fallback = document.getElementById('detailsImgFallback'); imageBox.querySelector('img')?.remove(); fallback.style.display = data.foto ? 'none' : '';
    if (data.foto) { const image = document.createElement('img'); image.src = data.foto; image.alt = data.nome || ''; imageBox.appendChild(image); }
    setOpen(true);
  }));
  close.addEventListener('click', () => setOpen(false));
  modal.addEventListener('mousedown', event => { if (event.target === modal) setOpen(false); });
  document.addEventListener('keydown', event => { if (event.key === 'Escape') setOpen(false); });
})();
