package com.abdetunes;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;
import java.io.*;

public class MPKminiplusExtension extends ControllerExtension
{
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

      host.getMidiInPort(0).createNoteInput("Notes");

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
      if (statusByte == ShortMidiMessage.PITCH_BEND){ // piano and button messages
         getHost().println("this is pitch bend");
      } else if ((statusByte & 0xF0) == ShortMidiMessage.NOTE_OFF){ // cylinders (?) and play/next buttons
         getHost().println("this is note off");
      } else if ((statusByte & 0xF0) == ShortMidiMessage.NOTE_ON){ // note modulators
         getHost().println("this is note on");
      } else if (statusByte == ShortMidiMessage.CONTROL_CHANGE){ // note modulators
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
