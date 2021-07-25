var q={};
function setScale(m,b,x,y,z) {
    var bone = m.getBone(b);
    !isNaN(x)?bone.setScaleX(x):'';
    !isNaN(y)?bone.setScaleY(y):'';
    !isNaN(z)?bone.setScaleZ(z):'';
}
function setRotation(m,b,x,y,z) {
    var bone = m.getBone(b);
    !isNaN(x)?bone.setRotationX(x):'';
    !isNaN(y)?bone.setRotationY(y):'';
    !isNaN(z)?bone.setRotationZ(z):'';
}
function setPosition(m,b,x,y,z) {
    var bone = m.getBone(b);
    !isNaN(x)?bone.setPositionX(x):'';
    !isNaN(y)?bone.setPositionY(y):'';
    !isNaN(z)?bone.setPositionZ(z):'';
}
function init(entity, model) {
	model.getBone("bone").physicalize(0.2, 50, 0.5, 0.5, 0.3);
    model.getBone("bone2").physicalize(0.2, 50, 0.5, 0.5, 0.3);
	
	setPosition(model,"builtin_chestplate_body_body",0,0,0)
	setPosition(model,"builtin_chestplate_right_right_arm",0,-2,0);
	setPosition(model,"builtin_chestplate_left_left_arm",0,-2,0);
	setPosition(model,"builtin_leggings_body_body",0,0,0);
	setScale(model,"builtin_helmet_head",1.2,1,1);
}