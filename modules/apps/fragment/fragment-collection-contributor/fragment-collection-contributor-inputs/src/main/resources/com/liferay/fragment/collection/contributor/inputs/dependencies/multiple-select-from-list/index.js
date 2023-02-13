const options = input.attributes.options || [];
const numberOfOptions = configuration.numberOfOptions;

const fieldSet = fragmentElement.querySelector('.multiselect-list-fieldset');

const allInputs = Array.from(
	fragmentElement.querySelectorAll('.custom-control-input')
);

if (layoutMode === 'edit') {
	allInputs.forEach((input) => {
		input.setAttribute('disabled', true);
	});
}

fieldSet.addEventListener('change', () => {
	const someInputIsChecked = allInputs.some((input) => input.checked);

	if (someInputIsChecked) {
		allInputs.forEach((input) => input.removeAttribute('required'));
	}
	else {
		allInputs.forEach((input) => input.setAttribute('required', true));
	}
});

if (numberOfOptions < options.length) {
	const button = fragmentElement.querySelector('.multiselect-list-button');

	const missionOptions = options.slice(numberOfOptions);

	const template = fragmentElement.querySelector(
		'.multiselect-list-option-template'
	);

	button.addEventListener('click', () => {
		missionOptions.forEach((option) => {
			const node = template.content.cloneNode(true);

			const input = node.querySelector('input');
			input.value = option.value;
			// eslint-disable-next-line no-undef
			input.id = `${fragmentEntryLinkNamespace}-checkbox-${option.value}`;

			if (layoutMode === 'edit') {
				input.setAttribute('disabled', true);
			}

			const label = node.querySelector('label');

			label.setAttribute(
				'for',
				// eslint-disable-next-line no-undef
				`${fragmentEntryLinkNamespace}-checkbox-${option.value}`
			);

			const text = node.querySelector('.custom-control-label-text');
			text.textContent = option.label;

			fieldSet.appendChild(node);
			allInputs.push(node);
		});

		fieldSet.removeChild(button);
	});
}
