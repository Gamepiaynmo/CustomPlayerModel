var blink, left_wing, right_wing;

function init(entity, model) {
	for (var i = 1; i <= 14; i++) {
		model.getBone("crystal_" + i).physicalize(0, 3, 0.8, 10, 0);
	}

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
	left_wing = model.getBone("wingLeft");
	right_wing = model.getBone("wingRight");
}

function update(entity, model) {
	// left_wing.setRotationY(Math.cos(entity.getAge() * 0.3) * 20 - 60);
	// right_wing.setRotationY(-Math.cos(entity.getAge() * 0.3) * 20 + 60);
}

function tick(entity, model) {
	blink.setVisible(entity.getAge() % 60 < 5);
}
