(() => {
  const selector = [
    '.upload-box > img', '.additional-photo img', '.product-preview-image img',
    '.gallery-main > img', '.gallery-thumbs img', '.product-image img',
    '.edit-main-image img', '.favorite-product-image > img',
    '.mini-product img', '.store-card > img', '.featured-main-image', '.featured-thumbs img',
    '.cart-item-image img', '.cart-modal-image img', '.commerce-image img',
    '.commerce-item img', '.favorite-card img', '.order-image img',
    '.detail-item-image img', '.seller-product-box img', '.seller-product-image img',
    '.history-image img', '.tracking-product-image img', '.rating-product-image img',
    '.report-product-image img'
  ].join(',');
  const photos = new Map();
  const refreshers = new WeakMap();
  let frame;
  function schedule() {
    if (frame) return;
    frame = requestAnimationFrame(() => {
      frame = null;
      photos.forEach((backdrop, img) => {
        if (!img.isConnected) {
          backdrop.remove();
          resize.unobserve(img);
          photos.delete(img);
          return;
        }
        backdrop.style.left = `${img.offsetLeft}px`;
        backdrop.style.top = `${img.offsetTop}px`;
        backdrop.style.width = `${img.offsetWidth}px`;
        backdrop.style.height = `${img.offsetHeight}px`;
        const style = getComputedStyle(img);
        backdrop.style.borderRadius = style.borderRadius;
        const left = parseFloat(style.borderLeftWidth) + parseFloat(style.paddingLeft);
        const top = parseFloat(style.borderTopWidth) + parseFloat(style.paddingTop);
        const width = img.clientWidth - parseFloat(style.paddingLeft) - parseFloat(style.paddingRight);
        const height = img.clientHeight - parseFloat(style.paddingTop) - parseFloat(style.paddingBottom);
        const ready = img.complete && img.naturalWidth > 0 && width > 0 && height > 0;
        const scale = ready ? Math.min(width / img.naturalWidth, height / img.naturalHeight) : 0;
        const photoWidth = img.naturalWidth * scale;
        const photoHeight = img.naturalHeight * scale;
        const hasSpace = ready && (width - photoWidth > 1 || height - photoHeight > 1);
        backdrop.hidden = !hasSpace;
        img.classList.toggle('product-photo-letterbox', hasSpace);
        if (hasSpace) {
          const x = left + (width - photoWidth) / 2;
          const y = top + (height - photoHeight) / 2;
          const right = left + width;
          const bottom = top + height;
          // Only paint the letterbox bands, never white or transparent pixels inside the photo.
          backdrop.style.clipPath = `polygon(evenodd, ${left}px ${top}px, ${right}px ${top}px, ${right}px ${bottom}px, ${left}px ${bottom}px, ${left}px ${top}px, ${x}px ${y}px, ${x}px ${y + photoHeight}px, ${x + photoWidth}px ${y + photoHeight}px, ${x + photoWidth}px ${y}px, ${x}px ${y}px)`;
        }
      });
    });
  }
  const resize = new ResizeObserver(schedule);
  function attach(img) {
    if (photos.has(img)) return;
    const parent = img.parentElement;
    if (getComputedStyle(parent).position === 'static') parent.classList.add('product-photo-host');
    if (getComputedStyle(img).position === 'static') img.classList.add('product-photo-static');
    const backdrop = document.createElement('span');
    backdrop.className = 'product-photo-backdrop';
    backdrop.setAttribute('aria-hidden', 'true');
    backdrop.hidden = true;
    parent.insertBefore(backdrop, img);
    img.classList.add('product-photo-foreground');
    photos.set(img, backdrop);
    const refresh = () => {
      const source = img.currentSrc || img.src;
      const ready = img.complete && img.naturalWidth > 0 && !!source;
      backdrop.hidden = true;
      if (ready) backdrop.style.setProperty('--photo-background', `url(${JSON.stringify(source)})`);
      schedule();
    };
    img.addEventListener('load', refresh);
    refreshers.set(img, refresh);
    img.addEventListener('error', () => { backdrop.hidden = true; });
    resize.observe(img);
    refresh();
  }
  document.querySelectorAll(selector).forEach(attach);
  new MutationObserver(records => {
    records.forEach(record => {
      if (record.type === 'attributes') {
        refreshers.get(record.target)?.();
      }
      record.addedNodes.forEach(node => {
        if (node.nodeType !== Node.ELEMENT_NODE) return;
        if (node.matches(selector)) attach(node);
        node.querySelectorAll(selector).forEach(attach);
      });
    });
    schedule();
  }).observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['src', 'srcset'] });
  window.addEventListener('resize', schedule);
  document.fonts.ready.then(schedule);
})();
