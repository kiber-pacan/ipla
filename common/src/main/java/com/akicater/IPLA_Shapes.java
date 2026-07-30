package com.akicater;


import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
#if MC_VER >= V1_19_4
import org.joml.Vector3f;
#else
import com.mojang.math.Vector3f;
#endif


public class IPLA_Shapes {
    private static VoxelShape shape(Vector3f a, Vector3f b) {
        return Shapes.create(
                Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z()),
                Math.max(a.x(), b.x()), Math.max(a.y(), b.y()), Math.max(a.z(), b.z())
        );
    }

    private static float[] get_sub_slot_offset(int index) {
        Direction direction = Direction.from3DDataValue(index / 4);

        int subslot = index % 4;
        int i = (direction.get3DDataValue() % 2 == 0) ? -1 : 1;

        float up_down = subslot > 1 ? -0.25f : 0.25f;
        float left_right = (index + 1) % 2 == 0 ? 0.25f : -0.25f;

        switch (direction) {
            case UP, DOWN -> {
                return new float[]{left_right, 0, up_down * i};
            } // Y XZ
            case NORTH, SOUTH -> {
                return new float[]{left_right * i, -up_down, 0};
            } // Z YX
            case WEST, EAST -> {
                return new float[]{0, -up_down, -left_right * i};
            } // X YZ
        }

        return new float[]{};
    }

    private static float[] get_dimensions(int type) {
        return switch (type) {
            // spike
            case 0 -> new float[]{0.0625f, 0.5f};
            // plate
            case 1 -> new float[]{0.25f, 0.0625f};
            // cuboid
            case 2 -> new float[]{0.25f, 0.5f};
            // slab
            case 3 -> new float[]{0.25f, 0.25f};
            // full_plate
            case 4 -> new float[]{0.5f, 0.0625f};

            default -> new float[]{0.25f, 0.5f};
        };
    }

    private static int get_type(Item item, boolean quad) {
        if (item instanceof BlockItem) {
            int type = IPLA.shapeTypeCache.get(((BlockItem) item).getBlock());

            return type == -1 ? (quad ? 1 : 4) : type;
        }
        return (quad ? 1 : 4);
    }

    private static void add(Vector3f vec, float val) {
        #if MC_VER >= V1_19_4
        vec.x += val;
        vec.y += val;
        vec.z += val;
        #else
        vec.add(val, val, val);
        #endif
    }

    private static void add(Vector3f vec, float x, float y, float z) {
        #if MC_VER >= V1_19_4
        vec.x += x;
        vec.y += y;
        vec.z += z;
        #else
        vec.add(x, y, z);
        #endif
    }

    /// 0 - spike
    /// 1 - plate
    /// 2 - cuboid
    /// 3 - slab
    public static VoxelShape get_shape(int index, Item item, boolean quad) {
        Direction direction = Direction.from3DDataValue(index / 4);
        Vector3f normal = direction.step();

        float normal_x = -normal.x();
        float normal_y = -normal.y();
        float normal_z = -normal.z();

        int i = (direction.get3DDataValue() % 2 == 0) ? -1 : 1;

        float mask_x = Math.abs(normal_x + i);
        float mask_y = Math.abs(normal_y + i);
        float mask_z = Math.abs(normal_z + i);

        float[] dimensions = get_dimensions(get_type(item, quad));
        float half_width = dimensions[0];
        float height = dimensions[1];


        Vector3f a = new Vector3f(
                -mask_x * half_width,
                -mask_y * half_width,
                -mask_z * half_width
        );
        Vector3f b = new Vector3f(
                mask_x * half_width + normal_x * height,
                mask_y * half_width + normal_y * height,
                mask_z * half_width + normal_z * height
        );

        add(a, normal_x * (0.5f - dimensions[1]), normal_y * (0.5f - dimensions[1]), normal_z * (0.5f - dimensions[1]));
        add(b, normal_x * (0.5f - dimensions[1]), normal_y * (0.5f - dimensions[1]), normal_z * (0.5f - dimensions[1]));

        add(a, 0.5f);
        add(b, 0.5f);

        if (quad) {
            float[] offset = get_sub_slot_offset(index);
            a.add(offset[0] * mask_x, offset[1] * mask_y, offset[2] * mask_z);
            b.add(offset[0] * mask_x, offset[1] * mask_y, offset[2] * mask_z);
        }


        return shape(a, b);
    }
}
