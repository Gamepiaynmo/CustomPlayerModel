var blink, sin, cos, _sin, _cos, left_wings, right_wings;

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
	sin = model.getBone("sinFloat");
	cos = model.getBone("cosFloat");
	_sin = model.getBone("_sinFloat");
	_cos = model.getBone("_cosFloat");
	left_wings = [
		model.getBone("wingLeft"),
		model.getBone("wingLeft2"),
		model.getBone("wingLeft3"),
		model.getBone("wingLeft4"),
	];
	right_wings = [
		model.getBone("wingRight"),
		model.getBone("wingRight2"),
		model.getBone("wingRight3"),
		model.getBone("wingRight4"),
	];
}

function update(entity, model) {
	sin.setPositionY(-4 + Math.sin(entity.getAge() / 9) * 2);
	cos.setPositionY(-14 + Math.cos(entity.getAge() / 10.5) * 2.5);
	_sin.setPositionY(-15 - Math.sin(entity.getAge() / 11) * 1.8);
	_cos.setPositionY(-2 - Math.cos(entity.getAge() / 10) * 2.2);
	for (var i in left_wings) {
		left_wings[i].setRotationY(Math.cos(entity.getAge() + i * 10) * 30 - 60);
		right_wings[i].setRotationY(-Math.cos(entity.getAge() + i * 10) * 30 + 60);
	}
}

function tick(entity, model) {
	blink.setVisible(entity.getAge() % 60 < 5);
}
