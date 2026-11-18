(() => {
  const form = document.querySelector('.edit-ad-form');
  const imageInput = document.getElementById('imageInput');
  const imageContainer = document.querySelector('.edit-main-image');
  const imagePlaceholder = document.getElementById('imagePlaceholder');
  const purposeInput = document.getElementById('finalidade');
  const valueInput = document.getElementById('valor');
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  const maxImageSize = 8 * 1024 * 1024;
  let previewUrl;
  const showToast = (message, type = 'error') => CampusFeedback.show(message, type);

  const showMainImage = (src, alt = 'Imagem do produto') => {
    let image = document.getElementById('mainImage');
    if (!image) {
      image = document.createElement('img');
      image.id = 'mainImage';
      imageContainer?.appendChild(image);
    }
    image.src = src;
    image.alt = alt;
    image.style.display = 'block';
    if (imagePlaceholder) imagePlaceholder.style.display = 'none';
  };

  document.querySelectorAll('[data-image-picker]').forEach(button => {
    button.addEventListener('click', () => imageInput?.click());
  });

  document.querySelectorAll('.secondary-photo-slot').forEach(slot => {
    const button = slot.querySelector('[data-secondary-picker]');
    const input = slot.querySelector('input[type="file"]');
    let acceptedFile;
    let url;
    button.addEventListener('click', () => input.click());
    input.addEventListener('change', () => {
      const file = input.files[0];
      if (!file) return;
      if (!allowedTypes.includes(file.type) || file.size > maxImageSize) {
        const previous = new DataTransfer();
        if (acceptedFile) previous.items.add(acceptedFile);
        input.files = previous.files;
        showToast('Use uma imagem JPG, PNG ou WEBP de até 8 MB.');
        return;
      }
      acceptedFile = file;
      if (url) URL.revokeObjectURL(url);
      url = URL.createObjectURL(file);
      let image = button.querySelector('img');
      if (!image) { image = document.createElement('img'); image.alt = 'Foto adicional do anúncio'; button.appendChild(image); }
      image.src = url;
      button.querySelector('svg')?.remove();
      button.classList.remove('add-photo');
      button.classList.add('existing-photo');
      slot.querySelector('small').textContent = 'Alteração pronta para salvar';
    });
    window.addEventListener('beforeunload', () => { if (url) URL.revokeObjectURL(url); });
  });

  imageInput?.addEventListener('change', () => {
    const file = imageInput.files?.[0];
    if (!file) return;
    if (!allowedTypes.includes(file.type)) {
      imageInput.value = '';
      showToast('Use uma imagem JPG, PNG ou WEBP.');
      return;
    }
    if (file.size > maxImageSize) {
      imageInput.value = '';
      showToast('A imagem deve ter no máximo 8 MB.');
      return;
    }
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = URL.createObjectURL(file);
    showMainImage(previewUrl, file.name);
    showToast('Nova imagem selecionada.', 'success');
  });

  document.querySelectorAll('[data-purpose-value]').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('[data-purpose-value]').forEach(item => {
        item.classList.remove('active');
        item.setAttribute('aria-pressed', 'false');
      });
      button.classList.add('active');
      button.setAttribute('aria-pressed', 'true');
      if (purposeInput) purposeInput.value = button.dataset.purposeValue;
    });
  });

  const parsePrice = value => {
    const clean = value.replace(/\s/g, '').replace(/R\$/gi, '');
    if (clean.includes(',') && clean.includes('.')) return clean.replace(/\./g, '').replace(',', '.');
    return clean.replace(',', '.').replace(/[^\d.-]/g, '');
  };

  valueInput?.addEventListener('blur', () => {
    const number = Number.parseFloat(parsePrice(valueInput.value));
    if (Number.isFinite(number)) valueInput.value = number.toFixed(2).replace('.', ',');
  });

  form?.addEventListener('submit', event => {
    const normalizedPrice = valueInput ? parsePrice(valueInput.value) : '';
    if (valueInput) valueInput.value = normalizedPrice;
    if (!form.checkValidity()) {
      event.preventDefault();
      form.reportValidity();
      return;
    }
    const number = Number.parseFloat(normalizedPrice);
    if (!Number.isFinite(number) || number <= 0) {
      event.preventDefault();
      showToast('Informe um preço maior que zero.');
    }
  });

  window.addEventListener('beforeunload', () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
  });
})();
