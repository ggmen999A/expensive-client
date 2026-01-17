package by.algorithm.alpha.system.utils.shader;

public interface IShader {

    String glsl();

    default String getName() {
        return "SHADERNONAME";
    }

}
