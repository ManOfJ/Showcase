package com.manofj.minecraft.moj_showcase.capability;

import java.util.NoSuchElementException;


public enum DisplayItemSetting {
    SCALE( 0, "scale" ),
    DEFAULT_ROTATION_PITCH( 1, "default_rotation_pitch" ),
    DEFAULT_ROTATION_YAW( 2, "default_rotation_yaw" ),
    DEFAULT_ROTATION_ROLL( 3, "default_rotation_roll" ),
    ROTATION_PITCH( 4, "rotation_pitch" ),
    ROTATION_YAW( 5, "rotation_yaw" ),
    ROTATION_ROLL( 6, "rotation_roll" ),
    STEP_ROTATION_PITCH( 7, "step_rotation_pitch" ),
    STEP_ROTATION_YAW( 8, "step_rotation_yaw" ),
    STEP_ROTATION_ROLL( 9, "step_rotation_roll" );

    public static final DisplayItemSetting[] VALUES = new DisplayItemSetting[ values().length ];

    static {
        for ( DisplayItemSetting e : values() )
        {
            if ( VALUES[ e.index ] != null )
            {
                throw new InternalError( "Index '" + e.index + "' is already registered." );
            }
            else
            {
                VALUES[ e.index ] = e;
            }
        }
    }

    private final int index;
    private final String name;

    DisplayItemSetting( int index, String name ) {
        this.index = index;
        this.name = name;
    }

    public static int size() {
        return VALUES.length;
    }

    public static DisplayItemSetting byIndex( int index ) {
        int actualIndex = index % VALUES.length;

        for ( DisplayItemSetting e : VALUES )
        {
            if ( e.index == actualIndex ) return e;
        }

        throw new NoSuchElementException( "index=" + index + ", actual=" + actualIndex );
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
