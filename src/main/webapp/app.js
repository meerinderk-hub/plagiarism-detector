(function () {
  var MAX = 10;
  var container = document.getElementById('docs');
  var addBtn = document.getElementById('addDoc');
  var form = document.getElementById('form');

  function countWords(text) {
    var m = text.trim().match(/\S+/g);
    return m ? m.length : 0;
  }

  function renumber() {
    var panels = container.querySelectorAll('.doc');
    panels.forEach(function (panel, i) {
      var n = i + 1;
      panel.querySelector('h2').textContent = 'Document ' + n;
      panel.querySelector('.title').name = 'title' + n;
      panel.querySelector('.text').name = 'text' + n;
      panel.querySelector('.remove').style.display = panels.length > 2 ? '' : 'none';
    });
    addBtn.disabled = panels.length >= MAX;
  }

  function createPanel() {
    var div = document.createElement('section');
    div.className = 'doc card';
    div.innerHTML =
      '<div class="doc-head"><h2></h2>' +
      '<button type="button" class="remove link">Remove</button></div>' +
      '<input class="title" type="text" maxlength="200" placeholder="Title (optional)">' +
      '<textarea class="text" rows="8" placeholder="Paste text here, or upload a .txt file below"></textarea>' +
      '<div class="doc-foot"><input class="file" type="file" accept=".txt,text/plain">' +
      '<span class="count">0 words</span></div>';

    var title = div.querySelector('.title');
    var text = div.querySelector('.text');
    var count = div.querySelector('.count');

    text.addEventListener('input', function () {
      count.textContent = countWords(text.value) + ' words';
    });

    div.querySelector('.file').addEventListener('change', function (e) {
      var file = e.target.files[0];
      if (!file) return;
      file.text().then(function (content) {
        text.value = content;
        count.textContent = countWords(content) + ' words';
        if (!title.value) title.value = file.name.replace(/\.[^.]+$/, '');
      });
    });

    div.querySelector('.remove').addEventListener('click', function () {
      div.remove();
      renumber();
    });

    container.appendChild(div);
    renumber();
  }

  addBtn.addEventListener('click', createPanel);

  form.addEventListener('submit', function (e) {
    var filled = 0;
    container.querySelectorAll('.text').forEach(function (t) {
      if (t.value.trim()) filled++;
    });
    if (filled < 2) {
      e.preventDefault();
      alert('Please enter or upload text for at least two documents.');
    }
  });

  createPanel();
  createPanel();
})();
