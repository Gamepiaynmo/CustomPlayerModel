var blink, crack, left_item, hide;

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
	crack = model.getBone("crack");
	left_item = model.getBone("left_item");
	hide = [
		model.getBone("bone12"),
		model.getBone("bone13"),
		model.getBone("bone14"),
		model.getBone("bone15"),
		model.getBone("bone16"),
		model.getBone("bone17"),
	]
}

function update(entity, model) {
	blink.setRotationY(entity.getAge());
	crack.setRotationY(entity.getAge());
	left_item.setVisible(entity.getLeftHandItem().isEmpty());

	var time = entity.getAge() % 120;
	alpha = time > 20 ? 0 : (1 + Math.abs(10 - time) / -10);
	for (var i in hide) {
		hide[i].setColorA(alpha);
	}
}
