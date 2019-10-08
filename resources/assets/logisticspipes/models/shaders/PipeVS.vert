#version 130
uniform mat4[10] transforms;

in int texture;
in int object;
in vec3 position;
in vec3 normal;
in vec2 uv;
in vec4 color;

flat out int textureOut;
out vec3 normalOut;
out vec2 uvOut;
out vec4 colorOut;

void main(){
    textureOut = texture;
    mat4 tr = transforms[object];
    normalOut = normalize(vec3(tr * vec4(normal, 1.0)));
    uvOut = uv;
    colorOut = color;

    gl_Position = gl_ModelViewProjectionMatrix * tr * vec4(position, 1.0);
}