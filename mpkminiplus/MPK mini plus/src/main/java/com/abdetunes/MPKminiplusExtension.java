package com.abdetunes;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.UserControlBank;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;
import java.io.*;

public class MPKminiplusExtension extends ControllerExtension
{
   int CC_RANGE_HI = 77;
   int CC_RANGE_LO = 70;
   UserControlBank userControls;


   protected MPKminiplusExtension(final MPKminiplusExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();      

      mTransport = host.createTransport();
      host.getMidiInPort(0).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi0(msg));

      // send notes back to bitwig
      host.getMidiInPort(0).createNoteInput("Notes");

      // CC
      userControls = host.createUserControls(CC_RANGE_HI - CC_RANGE_LO +1);
      for (int i = CC_RANGE_LO; i < CC_RANGE_HI; ++i){
         userControls.getControl(i - CC_RANGE_LO).setLabel("CC"+i);
      }


      host.getMidiInPort(0).setSysexCallback((String data) -> onSysex0(data));

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("MPK mini plus Initialized");
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("MPK mini plus Exited");
   }

   @Override
   public void flush()
   {
      // TODO Send any updates you need here.
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg) 
   {

      final ControllerHost host = getHost();
      getHost().println(String.format("new midi message with:  " +
                      "\n     status: 0x%x, " +
                      "\n     Data 1: 0x%x," +
                      "\n     Data 2: 0x%x, " +
                      "\n     Channel 0x%x",
              msg.getStatusByte(), msg.getData1(), msg.getData2(), msg.getChannel()));

      int statusByte = msg.getStatusByte();
      /*if (statusByte.isControlChange()){
         if(msg.getData1() >= CC_RANGE_LO && msg.getData2() <= CC_RANGE_HI){
            int index = msg.getData1() - CC_RANGE_LO;
            userControls.getControl(index).set(msg.getData2(), 128);
         }
      } else */
      if (statusByte == ShortMidiMessage.CONTROL_CHANGE){ // note modulators
         switch(msg.getData1()){
            case 0x73:
               mTransport.rewind();
               break;
            case 0x74:
               mTransport.fastForward();
               break;
            case 0x75:
               mTransport.stop();
               break;
            case 0x76:
               mTransport.play();
               break;
            case 0x77:
               mTransport.record();
               break;
            case 0x46:
               userControls.getControl(0).set(msg.getData2(), 128);
               break;
            case 0x47:
               userControls.getControl(1).set(msg.getData2(), 128);
               break;
            case 0x48:
               userControls.getControl(2).set(msg.getData2(), 128);
               break;
            case 0x49:
               userControls.getControl(3).set(msg.getData2(), 128);
               break;
            case 0x4a:
               userControls.getControl(4).set(msg.getData2(), 128);
               break;
            case 0x4b:
               userControls.getControl(5).set(msg.getData2(), 128);
               break;
            case 0x4c:
               userControls.getControl(6).set(msg.getData2(), 128);
               break;
            case 0x4d:
               userControls.getControl(7).set(msg.getData2(), 128);
               break;
         }
      }

      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data) 
   {
      // MMC Transport Controls:
      if (data.equals("f07f7f0605f7"))
            mTransport.rewind();
      else if (data.equals("f07f7f0604f7"))
            mTransport.fastForward();
      else if (data.equals("f07f7f0601f7"))
            mTransport.stop();
      else if (data.equals("f07f7f0602f7"))
            mTransport.play();
      else if (data.equals("f07f7f0606f7"))
            mTransport.record();
   }

   private Transport mTransport;
}
