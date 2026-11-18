(() => {
  const input = document.getElementById('fileInput');
  const box = document.getElementById('uploadBox');
  const add = document.getElementById('addPhoto');
  const items = [...document.querySelectorAll('.photo-item')];
  const form = input.form;
  const acceptedFiles = new WeakMap();
  const updateAdd = () => {
    add.disabled = items.every(item => item.dataset.savedUrl || item.querySelector('input').files.length);
  };
  const preview = (field, callback) => {
    const file = field.files[0];
    if (!file) return;
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type) || file.size > 8 * 1024 * 1024) {
      CampusFeedback.error(file.size > 8 * 1024 * 1024 ? 'A imagem deve ter no máximo 8 MB.' : 'Selecione uma imagem PNG, JPG ou WEBP.');
      const previous = new DataTransfer();
      if (acceptedFiles.has(field)) previous.items.add(acceptedFiles.get(field));
      field.files = previous.files;
      return;
    }
    const reader = new FileReader();
    reader.onload = event => {
      if (field.files[0] !== file) return;
      acceptedFiles.set(field, file);
      callback(event.target.result);
      updateAdd();
    };
    reader.readAsDataURL(file);
  };
  input.addEventListener('change', () => preview(input, src => {
    let img = document.getElementById('previewImage');
    if (!img) {
      img = document.createElement('img');
      img.id = 'previewImage';
      img.alt = 'Foto principal do produto';
      box.insertBefore(img, input);
    }
    img.src = src;
    box.classList.add('has-image');
  }));
  box.tabIndex = 0;
  box.addEventListener('keydown', event => {
    if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); input.click(); }
  });
  box.addEventListener('dragover', event => { event.preventDefault(); box.classList.add('dragover'); });
  box.addEventListener('dragleave', () => box.classList.remove('dragover'));
  box.addEventListener('drop', event => {
    event.preventDefault();
    box.classList.remove('dragover');
    if (event.dataTransfer.files.length) {
      const transfer = new DataTransfer();
      transfer.items.add(event.dataTransfer.files[0]);
      input.files = transfer.files;
      input.dispatchEvent(new Event('change'));
    }
  });
  items.forEach(item => {
    const field = item.querySelector('input');
    const label = item.querySelector('label');
    const remove = item.querySelector('.photo-remove');
    // Saved images are removed explicitly before choosing their replacement.
    label.addEventListener('click', event => { if (item.dataset.savedUrl) event.preventDefault(); });
    label.tabIndex = 0;
    label.addEventListener('keydown', event => {
      if ((event.key === 'Enter' || event.key === ' ') && !item.dataset.savedUrl) { event.preventDefault(); field.click(); }
    });
    field.addEventListener('change', () => preview(field, src => {
      let img = label.querySelector('img');
      if (!img) { img = document.createElement('img'); img.alt = 'Foto adicional'; label.prepend(img); }
      img.src = src;
      item.querySelector('.additional-photo').classList.add('is-filled');
      remove.hidden = false;
    }));
    remove.addEventListener('click', async () => {
      remove.disabled = true;
      try {
        if (item.dataset.savedUrl) {
          const data = new FormData();
          data.append('idProduto', form.elements.idProduto.value);
          data.append('urlFoto', item.dataset.savedUrl);
          const csrf = form.querySelector('input[name="_csrf"]');
          if (csrf) data.append(csrf.name, csrf.value);
          const response = await fetch('/anuncios/novo/fotos/remover', { method: 'POST', body: data });
          if (!response.ok || response.redirected) throw new Error('remove');
        }
        item.dataset.savedUrl = '';
        field.value = '';
        acceptedFiles.delete(field);
        label.querySelector('img')?.remove();
        item.querySelector('.additional-photo').classList.remove('is-filled');
        remove.hidden = true;
        updateAdd();
      } catch {
        CampusFeedback.error('Não foi possível excluir a foto. Tente novamente.');
      } finally { remove.disabled = false; }
    });
  });
  add.addEventListener('click', () => {
    const available = items.find(item => !item.dataset.savedUrl && !item.querySelector('input').files.length);
    if (available) available.querySelector('input').click();
  });
  updateAdd();
})();
