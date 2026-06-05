package uk.co.cablepost.ad_astra_cargo_rockets.cargo_rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;

public class T1CargoRocketEntityModel<T extends CargoRocketEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(AdAstraCargoRockets.MOD_ID, "cargo_rocket"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart nose;
    private final ModelPart fins;

    public T1CargoRocketEntityModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.nose = this.root.getChild("nose");
        this.fins = this.root.getChild("fins");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();

        PartDefinition r = rootDef.addOrReplaceChild("root",
                CubeListBuilder.create(), PartPose.offset(0f, 24f, 0f));

        // Main cylindrical body
        r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4f, -32f, -4f, 8f, 32f, 8f, new CubeDeformation(0f)),
                PartPose.offset(0f, 0f, 0f));

        // Nose cone
        r.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-3f, -36f, -3f, 6f, 4f, 6f, new CubeDeformation(0f)),
                PartPose.offset(0f, 0f, 0f));

        // Fins
        r.addOrReplaceChild("fins",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-8f, -12f, -1f, 4f, 12f, 2f, new CubeDeformation(0f))
                        .texOffs(32, 0)
                        .addBox(4f, -12f, -1f, 4f, 12f, 2f, new CubeDeformation(0f)),
                PartPose.offset(0f, 0f, 0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                                int packedLight, int packedOverlay,
                                float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
