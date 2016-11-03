package com.manofj.minecraft.moj_showcase.gui

import java.text.DecimalFormat

import scala.util.control.NonFatal

import com.google.common.base.Predicate
import org.lwjgl.input.Keyboard

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.util.ResourceLocation

import net.minecraftforge.fml.client.config.GuiButtonExt
import net.minecraftforge.fml.client.config.GuiSlider
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

import com.manofj.minecraft.moj_showcase.Showcase
import com.manofj.minecraft.moj_showcase.inventory.ShowcaseContainer
import com.manofj.minecraft.moj_showcase.network.UpdateSetting
import com.manofj.minecraft.moj_showcase.tileentity.ShowcaseBase


private object GuiShowcase {

  final val GUI_FRAME_TEXTURE  = new ResourceLocation( "textures/gui/demo_background.png" )
  final val SLOT_TEXTURE       = new ResourceLocation( "textures/gui/container/generic_54.png" )
  final val PREVIEW_BG_TEXTURE = new ResourceLocation( "textures/gui/container/horse.png" )
  final val TEXT_BG_TEXTURE    = new ResourceLocation( "textures/gui/widgets.png" )

  final val INVENTORY_AREA_WIDTH  = 162
  final val INVENTORY_AREA_HEIGHT = 142
  final val SETTINGS_AREA_WIDTH   = 168
  final val SETTINGS_AREA_HEIGHT  = 152

  final val DECIMAL_FORMAT    = new DecimalFormat( "##0.00##" )
  final val DECIMAL_VALIDATOR = """^[+-]?(0?|[1-9]\d{0,2})((?<=\d)\.\d{0,4}|\.\d{1,4}|(?!\.))$""".r.pattern

}

@SideOnly( Side.CLIENT )
class GuiShowcase( playerInventory: IInventory, showcase: ShowcaseBase, player: EntityPlayer )
  extends GuiContainer( new ShowcaseContainer( playerInventory, showcase, player ) )
  with    GuiSlider.ISlider
  with    GuiResponder
{
  import com.manofj.minecraft.moj_showcase.capability.{ DisplayItemSetting => Setting }
  import com.manofj.minecraft.moj_showcase.gui.GuiShowcase._


  var inventoryLeft  = 0
  var inventoryRight = 0
  var inventoryTop   = 0
  var settingsLeft   = 0
  var settingsRight  = 0
  var settingsTop    = 0
  var settingsBottom = 0

  var showcaseInvWidth  = 0
  var showcaseInvHeight = 0

  var currentItem = 0

  var previousButton: GuiButton = null
  var nextButton: GuiButton     = null
  var resetButton: GuiButton    = null
  var undoButton: GuiButton     = null
  var confirmButton: GuiButton  = null

  var scrollBar: GuiButton = null
  var scrollValue          = 0F

  var settingComponents = Map.empty[ Int, ( GuiSlider, GuiTextField ) ]

  val defaultSettings = showcase.defaultSettings
  var currentSettings = Map.empty[ Setting, Float ]
  var localSettings   = Map.empty[ Setting, Float ]


  {
    xSize = 7 + INVENTORY_AREA_WIDTH + 4 + SETTINGS_AREA_WIDTH + 7
    ySize = 17 + INVENTORY_AREA_HEIGHT + 7
    showcaseInvWidth  = ( showcase.getSizeInventory % 4 ) * 18
    showcaseInvHeight = ( ( showcase.getSizeInventory / 4 ) max 3 min 1 ) * 18

    assert( ySize == ( 7 + SETTINGS_AREA_HEIGHT + 7 ) )
  }

  private[ this ] def settingSliders = settingComponents.values.map( _._1 )

  private[ this ] def settingTexts   = settingComponents.values.map( _._2 )


  def updateSettingsCache(): Unit = {
    currentSettings = Setting.VALUES.map( x => x -> showcase.getFieldFloat( currentItem, x ) ).toMap
    localSettings   = currentSettings
  }

  def updateButtonState(): Unit = {
    previousButton.enabled = currentItem > 0
    nextButton.enabled     = ( currentItem + 1 ) < showcase.getSizeInventory
    resetButton.enabled    = localSettings != defaultSettings
    undoButton.enabled     = localSettings != currentSettings
    confirmButton.enabled  = undoButton.enabled
  }

  def updateSettingComponents(): Unit = {
    val beginIndex    = ( scrollValue * ( Setting.size - 6 ) ).toInt
    val endIndex      = beginIndex + 5
    val componentsTop = settingsTop + 16 + 2 + 1

    settingComponents.foreach { case ( i, ( slider, text ) ) =>
      val isEnable = ( i >= beginIndex ) && ( i <= endIndex )

      slider.enabled = isEnable
      slider.visible = isEnable
      text.setEnabled( isEnable )
      text.setVisible( isEnable )

      if ( isEnable ) {
        slider.yPosition = componentsTop + ( ( i - beginIndex ) * 19 )
        text.yPosition   = slider.yPosition + ( text.height - 8 ) / 2

        slider.setValue( localSettings( Setting.byIndex( i ) ) )
        text.setText( DECIMAL_FORMAT.format( slider.getValue ) )

      }
    }
  }


  override def initGui(): Unit = {
    super.initGui()

    inventoryLeft  = guiLeft + 7
    inventoryRight = inventoryLeft + INVENTORY_AREA_WIDTH
    inventoryTop   = guiTop  + 17
    settingsLeft   = inventoryRight + 4
    settingsRight  = settingsLeft + SETTINGS_AREA_WIDTH
    settingsTop    = guiTop + 7
    settingsBottom = settingsTop + SETTINGS_AREA_HEIGHT

    {
      val width = ( SETTINGS_AREA_WIDTH / 2 ) - 1

      previousButton = new GuiButtonExt( 1000, settingsLeft,          settingsTop, width, 16, Showcase.format( "gui.showcase.button_prev" ) )
      nextButton     = new GuiButtonExt( 1001, settingsRight - width, settingsTop, width, 16, Showcase.format( "gui.showcase.button_next" ) )

      buttonList.add( previousButton )
      buttonList.add( nextButton )
    }

    {
      val width = ( SETTINGS_AREA_WIDTH / 3 ) - 1

      resetButton   = new GuiButtonExt( 1002, settingsLeft,             settingsBottom - 16, width,     16, Showcase.format( "gui.showcase.button_reset" ) )
      undoButton    = new GuiButtonExt( 1003, settingsLeft + width + 1, settingsBottom - 16, width + 1, 16, Showcase.format( "gui.showcase.button_undo" ) )
      confirmButton = new GuiButtonExt( 1004, settingsRight - width,    settingsBottom - 16, width,     16, Showcase.format( "gui.showcase.button_confirm" ) )

      buttonList.add( resetButton )
      buttonList.add( undoButton )
      buttonList.add( confirmButton )
    }

    scrollBar = new GuiButtonExt( 2000, settingsRight - 14, settingsTop + 16 + 2, 14, 116, "" ) {
      var dragging = false

      def updateScroll( mouseX: Int, mouseY: Int ): Unit = {
        scrollValue = ( mouseY - ( this.yPosition + 4 ) ) / ( this.height - 24 ).toFloat
        scrollValue = scrollValue max 0F min 1F
        updateSettingComponents()
      }

      override def getHoverState( mouseOver: Boolean ): Int = 0

      override def mousePressed( mc: Minecraft, mouseX: Int, mouseY: Int ): Boolean = {
        if ( !super.mousePressed( mc, mouseX, mouseY ) ) false
        else {
          updateScroll( mouseX, mouseY )
          this.dragging = true
          true
        }
      }

      override def mouseDragged( mc: Minecraft, mouseX: Int, mouseY: Int ): Unit = {
        if ( this.visible ) {
          if ( this.dragging ) {
            updateScroll( mouseX, mouseY )
          }
          val scrollBoxY = this.yPosition + ( scrollValue * ( this.height - 48 ) ).toInt

          GlStateManager.color( 1F, 1F, 1F, 1F )
          GuiUtils.drawContinuousTexturedBox( this.xPosition, scrollBoxY, 0, 66, 14, 48, 200, 20, 2, 3, 2, 2, zLevel )
        }
      }

      override def mouseReleased( mouseX: Int, mouseY: Int ): Unit = this.dragging = false

    }
    buttonList.add( scrollBar )

    settingComponents = {
      val builder = Map.newBuilder[ Int, ( GuiSlider, GuiTextField ) ]

      val ranges = Map(
        Setting.SCALE                  -> ( 0.1F -> 64F ),
        Setting.DEFAULT_ROTATION_PITCH -> (   0F -> 359.9999F ),
        Setting.DEFAULT_ROTATION_YAW   -> (   0F -> 359.9999F ),
        Setting.DEFAULT_ROTATION_ROLL  -> (   0F -> 359.9999F ),
        Setting.ROTATION_PITCH         -> (   0F -> 359.9999F ),
        Setting.ROTATION_YAW           -> (   0F -> 359.9999F ),
        Setting.ROTATION_ROLL          -> (   0F -> 359.9999F ),
        Setting.STEP_ROTATION_PITCH    -> ( -64F -> 64F ),
        Setting.STEP_ROTATION_YAW      -> ( -64F -> 64F ),
        Setting.STEP_ROTATION_ROLL     -> ( -64F -> 64F ) )

      val componentsLeft = settingsLeft + 1

      val ( sliderWidth, textWidth ) = {
        val sw = Setting.VALUES
          .map( x => Showcase.message( showcase.getFieldName( x ) ) )
          .map( fontRendererObj.getStringWidth( _ ) + 8 )
          .max max 96

        val tw = ( fontRendererObj.getStringWidth( "-99.9999_" ) + 8 ) min
                 ( SETTINGS_AREA_WIDTH - sw - scrollBar.width )

        ( if ( sw + tw + scrollBar.width == SETTINGS_AREA_WIDTH ) sw else SETTINGS_AREA_WIDTH - tw - scrollBar.width, tw )
      }

      Setting.VALUES.foreach { setting =>
        val i   = setting.getIndex
        val min = ranges( setting )._1
        val max = ranges( setting )._2

        val slider = new GuiSlider( i, componentsLeft, 0, sliderWidth, 19, "", "", min, max, 0, false, true, this ) {
          this.displayString = Showcase.message( showcase.getFieldName( setting ) )

          override def mouseDragged( mc: Minecraft, mouseX: Int, mouseY: Int ): Unit = {
            if ( this.visible ) {
              if ( this.dragging ) {
                this.sliderValue = ( mouseX - ( this.xPosition + 4 ) ) / ( this.width - 8 ).toFloat
                this.updateSlider()
              }
              val x = this.xPosition + ( this.sliderValue * ( this.width - 8 ) ).toInt

              GlStateManager.color( 1F, 1F, 1F, 1F )
              GuiUtils.drawContinuousTexturedBox( x, this.yPosition, 0, 66, 8, this.height, 200, 20, 2, 3, 2, 2, this.zLevel )
            }
          }

          override def updateSlider(): Unit = {
            this.sliderValue = this.sliderValue max 0F min 1F
            this.parent.onChangeSliderValue( this )
        } }
        buttonList.add( slider )

        val textField = new GuiTextField( i, fontRendererObj, componentsLeft + sliderWidth + 4, 0, textWidth, 19 )
        textField.setEnableBackgroundDrawing( false )
        textField.setGuiResponder( this )
        textField.setValidator( new Predicate[ String ] {
          override def apply( s: String ): Boolean = {
            if ( s.isEmpty ) true
            else if ( !DECIMAL_VALIDATOR.matcher( s ).matches ) false
            else
              try {
                val v = s.toFloat
                ( v >= min ) && ( v <= max )
              }
              catch {
                case NonFatal( _ ) => false
          } }
        } )

        builder += i -> ( slider, textField )
      }

      builder.result
    }

    updateSettingsCache()
    updateButtonState()
    updateSettingComponents()
  }

  override def actionPerformed( button: GuiButton ): Unit = {
    button.id match {
      case 1000 => // prev item
        currentItem -= 1
        updateSettingsCache()
        updateButtonState()
        updateSettingComponents()

      case 1001 => // next item
        currentItem += 1
        updateSettingsCache()
        updateButtonState()
        updateSettingComponents()

      case 1002 => // reset to default
        localSettings = defaultSettings
        updateButtonState()
        updateSettingComponents()

      case 1003 => // undo changes
        localSettings = currentSettings
        updateButtonState()
        updateSettingComponents()

      case 1004 => // confirm
        Showcase.sendToServer( new UpdateSetting(
          localSettings
            .withFilter( x => currentSettings( x._1 ) != x._2 )
            .map( x => ( showcase.getFieldId( currentItem, x._1 ), x._2 ) )
        ) )
        currentSettings = localSettings
        updateButtonState()
        updateSettingComponents()

      case _ =>
    }
  }

  override def drawGuiContainerBackgroundLayer( partialTicks: Float, mouseX: Int, mouseY: Int ): Unit = {

    GuiUtils.drawContinuousTexturedBox( GUI_FRAME_TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 248, 166, 4, zLevel )
    GuiUtils.drawContinuousTexturedBox( SLOT_TEXTURE, inventoryLeft, inventoryTop, 7, 17, showcaseInvWidth, showcaseInvHeight, 72, 54, 0, zLevel )
    GuiUtils.drawContinuousTexturedBox( inventoryLeft, guiTop + 83, 7, 139, 162, 76, 162, 76, 0, zLevel )
    GuiUtils.drawContinuousTexturedBox( PREVIEW_BG_TEXTURE, inventoryRight - 54, inventoryTop, 25, 17, 54, 54, 54, 54, 0, zLevel )
    GuiUtils.drawContinuousTexturedBox( settingsLeft, settingsTop + 16 + 2, 79, 17, SETTINGS_AREA_WIDTH - 14, 116, 90, 54, 1, zLevel )

    settingComponents.values.withFilter( _._1.visible ).foreach { case ( s, t ) =>
      val ( x, y ) = ( s.xPosition + s.width, s.yPosition )
      val ( w, h ) = ( t.width, t.height )
      GuiUtils.drawContinuousTexturedBox( TEXT_BG_TEXTURE, x, y, 0, 46, w - 2, h, 200, 20, 1, zLevel )
    }

    GlStateManager.color( 1F, 1F, 1F, 1F )
    settingTexts.foreach( _.drawTextBox() )

    Option( showcase.getStackInSlot( currentItem ) ).foreach { item =>
      GlStateManager.pushMatrix()
      GlStateManager.disableLighting()

      GlStateManager.translate( inventoryRight - 27, inventoryTop + 27, 50F )
      GlStateManager.scale( -50F, 50F, 50F )
      GlStateManager.rotate( 180F, 0F, 0F, 1F )
      GlStateManager.rotate( 180F, 0F, 1F, 0F )

      val scale = localSettings( Setting.SCALE )
      if ( scale <= 1.25 ) {
        GlStateManager.scale( -scale, scale, scale )
      }
      else {
        //TODO: 大きすぎるので描画しません画像
        GlStateManager.scale( -1.25F, 1.25F, 1.25F )
      }

      GlStateManager.rotate( localSettings( Setting.DEFAULT_ROTATION_PITCH ), 1F, 0F, 0F )
      GlStateManager.rotate( localSettings( Setting.DEFAULT_ROTATION_YAW ), 0F, 1F, 0F )
      GlStateManager.rotate( localSettings( Setting.DEFAULT_ROTATION_ROLL ), 0F, 0F, 1F )

      GlStateManager.rotate( localSettings( Setting.ROTATION_PITCH ), 1F, 0F, 0F )
      GlStateManager.rotate( localSettings( Setting.ROTATION_YAW ), 0F, 1F, 0F )
      GlStateManager.rotate( localSettings( Setting.ROTATION_ROLL ), 0F, 0F, 1F )

      GlStateManager.translate( 0.0F, 0.0F, 0.0F )

      GlStateManager.pushAttrib()
      RenderHelper.enableStandardItemLighting()
      mc.getRenderItem.renderItem( item, TransformType.FIXED )
      RenderHelper.disableStandardItemLighting()
      GlStateManager.popAttrib()
      GlStateManager.enableLighting()
      GlStateManager.popMatrix()
    }

  }

  override def updateScreen(): Unit = {
    super.updateScreen()
    settingTexts.foreach( _.updateCursorCounter() )
  }

  override def keyTyped( typedChar: Char, keyCode: Int ): Unit = {
    super.keyTyped( typedChar, keyCode )

    keyCode match {
      case Keyboard.KEY_RETURN =>
        settingTexts.foreach( _.setFocused( false ) )
        updateSettingComponents()

      case _ =>
        settingTexts.foreach( _.textboxKeyTyped( typedChar, keyCode ) )
    }
  }

  override def mouseClicked( mouseX: Int, mouseY: Int, mouseButton: Int ): Unit = {
    super.mouseClicked( mouseX, mouseY, mouseButton )
    settingTexts.foreach( _.mouseClicked( mouseX, mouseY, mouseButton ) )
  }

  override def onChangeSliderValue( slider: GuiSlider ): Unit = {
    val v = slider.getValue

    localSettings += Setting.byIndex( slider.id ) -> v.toFloat
    settingComponents( slider.id )._2.setText( DECIMAL_FORMAT.format( v ) )

    updateButtonState()
  }

  override def setEntryValue( id: Int, value: Boolean ): Unit = {}
  override def setEntryValue( id: Int, value: Float ): Unit = {}

  override def setEntryValue( id: Int, value: String ): Unit = {
    val v = if ( value.isEmpty ) 0F else value.toFloat

    localSettings += Setting.byIndex( id ) -> v
    settingComponents( id )._1.setValue( v )

    updateButtonState()
  }
}
