package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.module.Module

object ItemScale: Module("item-scale") {
    private val show_first_person by setting("show-first-person", true)

    private val instant_axe by setting("instant-axe", false)
    private val instant_axe_scale by setting("instant-axe-scale", 1f, 0.1f..5f)
        .visibility { instant_axe }
    private val instant_axe_x by setting("instant-axe-x-rotation", 0f, -10f..10f)
        .visibility { instant_axe }
    private val instant_axe_y by setting("instant-axe-y-rotation", 0f, -10f..10f)
        .visibility { instant_axe }
    private val instant_axe_z by setting("instant-axe-z-rotation", 0f, -10f..10f)
        .visibility { instant_axe }

    private val kb_ball by setting("kb-ball", false)
    private val kb_ball_scale by setting("kb-ball-scale", 1f, 0.1f..5f)
       .visibility { kb_ball }
    private val kb_ball_x by setting("kb-ball-x-rotation", 0f, -10f..10f)
       .visibility { kb_ball }
    private val kb_ball_y by setting("kb-ball-y-rotation", 0f, -10f..10f)
       .visibility { kb_ball }
    private val kb_ball_z by setting("kb-ball-z-rotation", 0f, -10f..10f)
       .visibility { kb_ball }

    private val totem by setting("totem", false)
    private val totem_scale by setting("totem-scale", 1f, 0.1f..5f)
        .visibility { totem }
    private val totem_x by setting("totem-x-rotation", 0f, -10f..10f)
        .visibility { totem }
    private val totem_y by setting("totem-y-rotation", 0f, -10f..10f)
        .visibility { totem }
    private val totem_z by setting("totem-z-rotation", 0f, -10f..10f)
        .visibility { totem }

    private val enchanted_golden_apple by setting("enchanted-golden-apple", false)
    private val enchanted_golden_apple_scale by setting("enchanted-golden-apple-scale", 1f, 0.1f..5f)
        .visibility { enchanted_golden_apple }
    private val enchanted_golden_apple_x by setting("enchanted-golden-apple-x-rotation", 0f, -10f..10f)
        .visibility { enchanted_golden_apple }
    private val enchanted_golden_apple_y by setting("enchanted-golden-apple-y-rotation", 0f, -10f..10f)
        .visibility { enchanted_golden_apple }
    private val enchanted_golden_apple_z by setting("enchanted-golden-apple-z-rotation", 0f, -10f..10f)
        .visibility { enchanted_golden_apple }

    private val power_v_bow by setting("power-v-bow", false)
    private val power_v_bow_scale by setting("power-v-bow-scale", 1f, 0.1f..5f)
        .visibility { power_v_bow }
    private val power_v_bow_x by setting("power-v-bow-x-rotation", 0f, -10f..10f)
        .visibility { power_v_bow }
    private val power_v_bow_y by setting("power-v-bow-y-rotation", 0f, -10f..10f)
        .visibility { power_v_bow }
    private val power_v_bow_z by setting("power-v-bow-z-rotation", 0f, -10f..10f)
        .visibility { power_v_bow }

    private val punch_iii_bow by setting("punch-iii-bow", false)
    private val punch_iii_bow_scale by setting("punch-iii-bow-scale", 1f, 0.1f..5f)
        .visibility { punch_iii_bow }
    private val punch_iii_bow_x by setting("punch-iii-bow-x-rotation", 0f, -10f..10f)
        .visibility { punch_iii_bow }
    private val punch_iii_bow_y by setting("punch-iii-bow-y-rotation", 0f, -10f..10f)
        .visibility { punch_iii_bow }
    private val punch_iii_bow_z by setting("punch-iii-bow-z-rotation", 0f, -10f..10f)
        .visibility { punch_iii_bow }

    private val flame_bow by setting("flame-bow", false)
    private val flame_bow_scale by setting("flame-bow-scale", 1f, 0.1f..5f)
       .visibility { flame_bow }
    private val flame_bow_x by setting("flame-bow-x-rotation", 0f, -10f..10f)
       .visibility { flame_bow }
    private val flame_bow_y by setting("flame-bow-y-rotation", 0f, -10f..10f)
       .visibility { flame_bow }
    private val flame_bow_z by setting("flame-bow-z-rotation", 0f, -10f..10f)
       .visibility { flame_bow }

    private val end_crystal by setting("end-crystal", false)
    private val end_crystal_scale by setting("end-crystal-scale", 1f, 0.1f..5f)
       .visibility { end_crystal }
    private val end_crystal_x by setting("end-crystal-x-rotation", 0f, -10f..10f)
       .visibility { end_crystal }
    private val end_crystal_y by setting("end-crystal-y-rotation", 0f, -10f..10f)
       .visibility { end_crystal }
    private val end_crystal_z by setting("end-crystal-z-rotation", 0f, -10f..10f)
       .visibility { end_crystal }

    private val sharp_viii by setting("sharp-viii", false)
    private val sharp_viii_scale by setting("sharp-viii-scale", 1f, 0.1f..5f)
       .visibility { sharp_viii }
    private val sharp_viii_x by setting("sharp-viii-x-rotation", 0f, -10f..10f)
       .visibility { sharp_viii }
    private val sharp_viii_y by setting("sharp-viii-y-rotation", 0f, -10f..10f)
       .visibility { sharp_viii }
    private val sharp_viii_z by setting("sharp-viii-z-rotation", 0f, -10f..10f)
         .visibility { sharp_viii }
}
