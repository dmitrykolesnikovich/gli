package gli_

import glm_.vec3.Vec3i
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.nio.file.Files

class coreLoadGen1dArray : StringSpec() {

    init {

        "load 1d array" {

            for (format in Format.FIRST .. Format.LAST) {

                val target = Target._1D_ARRAY

                if ((format.isCompressed && (target.isTarget1d || target == Target._3D)) || target.isTargetRect)
                    continue

                val layers = if (target.isTargetArray) 2 else 1
                val faces = if (target.isTargetCube) 6 else 1
                val blockExtent = format.blockExtend

                val texture = Texture(target, format, blockExtent * Vec3i(blockExtent.y, blockExtent.x, 1), layers, faces, 2)
                texture.clear()

                "test1d_array.dds".let {
                    gli.save(texture, it)
                    val textureDDS = gli.load(it)
                    texture shouldBe textureDDS
                    Files.delete(pathOf(it))
                }

                "test1d_array.ktx".let {
                    gli.save(texture, it)
                    val textureKTX = gli.load(it)
                    texture shouldBe textureKTX
                    Files.delete(pathOf(it))
                }

                "test1d_array.kmg".let {
                    gli.save(texture, it)
                    val textureKMG = gli.load(it)
                    texture shouldBe textureKMG
                    Files.delete(pathOf(it))
                }
            }
        }
    }
}