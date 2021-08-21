var blink, tail;

function init(entity, model) {
	var scale = 0.75;
	var bones = [
		model.getBone("builtin_chestplate_body_body"),
		model.getBone("builtin_chestplate_left_left_arm"),
		model.getBone("builtin_chestplate_right_right_arm"),
		model.getBone("builtin_item_left_left_arm"),
		model.getBone("builtin_item_right_right_arm"),
		model.getBone("builtin_leggings_body_body"),
		model.getBone("builtin_leggings_left_left_leg"),
		model.getBone("builtin_leggings_right_right_leg"),
		model.getBone("builtin_boots_left_left_leg"),
		model.getBone("builtin_boots_right_right_leg"),
		model.getBone("builtin_cape_body"),
		model.getBone("builtin_elytra_none"),
	];

	for (var i in bones) {
		bones[i].setScaleX(scale);
		bones[i].setScaleY(scale);
		bones[i].setScaleZ(scale);
	}

	bones[0].setScaleZ(1.05);

	blink = model.getBone("blink");
	tail = model.getBone("tail");
}

function update(entity, model) {
	tail.setRotationX(Math.sin(entity.getAge() * 0.2) * 3);
	tail.setRotationZ(Math.cos(entity.getAge() * 0.2) * 6);
}

function tick(entity, model) {
	blink.setVisible(entity.getAge() % 60 < 5);
}
