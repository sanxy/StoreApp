package com.general.files.thermalPrint;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.internal.Util;

public class GenerateOrderBill {

    Activity mContext;
    GeneralFunctions generalFun;
    private static GenerateOrderBill instance = null;

    public void init() {
        mContext = MyApp.getInstance().getCurrentAct();
        generalFun = new GeneralFunctions(mContext);

    }

    public void init(Activity activity) {
        mContext = activity;
        generalFun = new GeneralFunctions(mContext);

    }


    public static GenerateOrderBill getInstance() {
        if (instance == null) {
            instance = new GenerateOrderBill();
            MyApp.generateOrderBill = instance;
        }
        return instance;
    }


    public void printBill(String[] item, ArrayList<HashMap<String, String>> fareList, HashMap<String, String> reqiuredDetails) {
        OutputStream opstream = null;
        try {
            opstream = MyApp.btsocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyApp.outputStream = opstream;

        //print command
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MyApp.outputStream = MyApp.btsocket.getOutputStream();
            byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
            MyApp.outputStream.write(printformat);

            printData(item, fareList, reqiuredDetails);

            MyApp.outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void printData(String[] items, ArrayList<HashMap<String, String>> fareList, HashMap<String, String> reqiuredDetails) {

        String startChar = "";
        String midText = "";
        String midText1 = "";
        String midText2 = "";
        String totalBill = "";
        int align = -1;
        int align2 = -1;
        int headerCount=0;

        for (String item : items) {

            if (!item.isEmpty()) {


                if (startChar.isEmpty()) {
                    System.out.println("item = startChar" + startChar);
                    startChar = item;
                } else if (!item.equalsIgnoreCase("NL")) {
                    if (startChar.equalsIgnoreCase("CENTER")) {
                        headerCount++;
                        align = 1;
                        midText = item;
                    }

                    if (startChar.equalsIgnoreCase("LEFT")) {
                        align = generalFun.isRTLmode()?2:0;

                        if (midText1.isEmpty()) {
                            midText1 = item;
                        } else {
                            midText2 = item;
                        }
                    }


                    if (startChar.equalsIgnoreCase("RIGHT")) {

                        align2 = generalFun.isRTLmode()?0:2;

                        if (midText1.isEmpty()) {
                            midText1 = item;
                        } else {
                            midText2 = item;
                        }
                    }

                    if (startChar.equalsIgnoreCase("LEFT") && item.equalsIgnoreCase("RIGHT")) {
                        startChar = item;
                    } else if (startChar.equalsIgnoreCase("RIGHT") && item.equalsIgnoreCase("LEFT")) {
                        startChar = item;
                    }


                }


                boolean isLast=items[items.length - 1] == item;
                if (item.equalsIgnoreCase("NL") || isLast) {

                    System.out.println("item = " + item);
                    System.out.println("item = align" + align);
                    System.out.println("item = align2" + align2);
                    System.out.println("item = midText" + midText);
                    System.out.println("item = midText1" + midText1);
                    System.out.println("item = midText2" + midText2);

                    if (!midText.isEmpty()) {

                        String txt = "";
                        if (reqiuredDetails.containsKey(midText)) {
                            midText = reqiuredDetails.get(midText);
                        }

                        int fontSize = 0;

                        if (headerCount==1) {
                            fontSize = 1;
                        } else if (isLast)
                        {
                            fontSize = 1;
                        }

                        printCustom(midText, fontSize, align);

                        if (headerCount!=1 /*&& !isLast*/) {
                            printNewLine();
                        }

                        if (isLast)
                        {
                            printNewLine();
                            printNewLine();
                        }
                    }

                    if (!midText1.isEmpty()) {
                        String txt = "";

                        if (reqiuredDetails.containsKey(midText2)) {
                            midText2 = reqiuredDetails.get(midText2);
                        }


                        if (align2 != -1) {
                            if (midText1.equalsIgnoreCase("ITEM_NAME") && midText2.equalsIgnoreCase("ITEM_QTY")) {

                                for (int i = 0; i < fareList.size(); i++) {
                                    midText1 = "";
                                    midText2 = "";
                                    HashMap<String, String> itemData = fareList.get(i);

                                    midText1 = itemData.get("MenuItem");
                                    midText2 = "x " + itemData.get("iQty");


                                    String[] stringArr = splitToNChar(midText1, 26);
                                    for (int i1 = 0; i1 < stringArr.length; i1++) {
                                        String txt1 = stringArr[i1];
                                        String txt2 = i1 == stringArr.length - 1 ? midText2 : "   ";

                                        Log.d("PRINT_ERROR", "txt1" + txt1 );
                                        Log.d("PRINT_ERROR", "txt2" + txt2 );

                                        leftRightAlign(txt1,txt2, 1, align, 1, align2,generalFun.isRTLmode());

                                        if (Utils.checkText(itemData.get("MenuItemToppings")) && Utils.checkText(itemData.get("MenuItemToppings")))
                                        {
//                                            printNewLine();
//                                            printCustom(itemData.get("MenuItemToppings"), 1, align);
                                            leftRightAlign(itemData.get("MenuItemToppings")+","+itemData.get("MenuItemOptions"),"", 0, align, 0, align2,generalFun.isRTLmode());
//                                            printNewLine();
                                        }
                                        else if (Utils.checkText(itemData.get("MenuItemToppings")))
                                        {
//                                            printNewLine();
//                                            printCustom(itemData.get("MenuItemToppings"), 1, align);
                                            leftRightAlign(itemData.get("MenuItemToppings"),"", 0, align, 0, align2,generalFun.isRTLmode());
//                                            printNewLine();
                                        }

                                        else if (Utils.checkText(itemData.get("MenuItemOptions")))
                                        {
//                                            printNewLine();
//                                            printCustom(itemData.get("MenuItemOptions"), 1, align);
                                            leftRightAlign(itemData.get("MenuItemOptions"),"", 0, align, 0, align2,generalFun.isRTLmode());
//                                            printNewLine();
                                        }
                                    }

                                }

                            } else if (!midText1.isEmpty() && !midText2.isEmpty()) {
                                printText(leftRightAlign(midText1, midText2,generalFun.isRTLmode()));
                            }
                        } else {
                            if (generalFun.isRTLmode())
                            {
                                printCustom(midText2+ " " + midText1, 0, align);
                            }else
                            {
                                printCustom(midText1 + " " + midText2, 0, align);
                            }
                        }


                    }

                    startChar = "";
                    midText = "";
                    midText1 = "";
                    midText2 = "";
                    align = -1;
                    align2 = -1;

                } else if (item.equalsIgnoreCase("BREAK_LINE")) {
                    printCustom(new String(new char[42]).replace("\0", "."), 0, 1);

                }
            }

        }

    }


    /**
     * Split text into n number of characters.
     *
     * @param text the text to be split.
     * @param size the split size.
     * @return an array of the split text.
     */
    private static String[] splitToNChar(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        return parts.toArray(new String[0]);
    }

    //print custom
    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    MyApp.outputStream.write(cc);
                    break;
                case 1:
                    MyApp.outputStream.write(bb);
                    break;
                case 2:
                    MyApp.outputStream.write(bb2);
                    break;
                case 3:
                    MyApp.outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            MyApp.outputStream.write(msg.getBytes());
            MyApp.outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void writePrint(byte[] align, String msg) {
        try {
            MyApp.outputStream.write(align);
            String space = "   ";
            int l = msg.length();
            if (l < 31) {
                for (int x = 31 - l; x >= 0; x--) {
                    space = space + " ";
                }
            }
            msg = msg.replace(" : ", space);
            MyApp.outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void writePrint(int l, String msg) {
        try {
            String space = "   ";
            if (l < 31) {
                for (int x = 31 - l; x >= 0; x--) {
                    space = space + " ";
                }
            }

            Log.d("PRINT_ERROR", "space" + space.length() + " msg" + msg.length());
            msg = space + msg;

            MyApp.outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void writePrint(int l, String msg,boolean isRtl) {
        int length=isRtl?31:38;

        try {
            String space = "   ";
            if (l < length) {
                for (int x = length - l; x >= 0; x--) {
                    space = space + " ";
                }
            }

            Log.d("PRINT_ERROR", "space" + space.length() + " msg" + msg.length());
            msg = space + msg;

            MyApp.outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print photo
    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
                    img);
            if (bmp != null) {
                byte[] command = PrintUtils.decodeBitmap(bmp);
                MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print unicode
    public void printUnicode() {
        try {
            MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(PrintUtils.UNICODE_TEXT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //print new line
    private void printNewLine() {
        try {
            MyApp.outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void resetPrint() {
        try {
            MyApp.outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            MyApp.outputStream.write(PrinterCommands.FS_FONT_ALIGN);
            MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            MyApp.outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
            MyApp.outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print text
    public void printText(String msg) {
        try {
            // Print normal text

            MyApp.outputStream.write(msg.getBytes());
            MyApp.outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    public void printText(byte[] msg) {
        try {
            // Print normal text
            MyApp.outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String leftRightAlign(String str1, String str2) {
        String ans = str1 + str2;
        if (ans.length() < 31) {
            int n = (31 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }

    private String leftRightAlign(String str1, String str2,boolean isRtl) {
        String ans = str1 + str2;
//        int length=isRtl?31:25;
//        int length=isRtl?31:25;
        int length=26;
        if (ans.length() < length) {
            int n = (length - str1.length() + str2.length());
            ans = (isRtl?str2:str1) + new String(new char[n]).replace("\0", " ") + (isRtl?str1:str2);
        }
        return ans;
    }


    private void leftRightAlign(String str1, String str2, int size, int align, int size2, int align2) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large textString ans = str1 + str2;


        //Print config "mode"
        byte[] cc1 = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb1 = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb21 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb31 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    MyApp.outputStream.write(cc);
                    break;
                case 1:
                    MyApp.outputStream.write(bb);
                    break;
                case 2:
                    MyApp.outputStream.write(bb2);
                    break;
                case 3:
                    MyApp.outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }

            MyApp.outputStream.write(str1.getBytes());
            //  MyApp.outputStream.write(PrinterCommands.LF);

            Log.d("PRINT_ERROR", "str1" + str1 + " length" + str1.length());

            switch (size2) {
                case 0:
                    MyApp.outputStream.write(cc1);
                    break;
                case 1:
                    MyApp.outputStream.write(bb1);
                    break;
                case 2:
                    MyApp.outputStream.write(bb21);
                    break;
                case 3:
                    MyApp.outputStream.write(bb31);
                    break;
            }

            switch (align2) {
                case 0:
                    //left align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }

          /*  if (str1.length() < 25) {
                int n = (31 - (str1.length() + str2.length()));
                str2 = new String(new char[n]).replace("\0", " ") + str2;
            }
                        MyApp.outputStream.write(str2.getBytes());

            */
            Log.d("PRINT_ERROR", "str2" + str2 + " length" + str2.length());

            writePrint((str1 + str2).length(), str2);


            MyApp.outputStream.write(PrinterCommands.LF);


            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void leftRightAlign(String str1, String str2, int size, int align, int size2, int align2,boolean isRtl) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large textString ans = str1 + str2;


        //Print config "mode"
        byte[] cc1 = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb1 = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb21 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb31 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    MyApp.outputStream.write(cc);
                    break;
                case 1:
                    MyApp.outputStream.write(bb);
                    break;
                case 2:
                    MyApp.outputStream.write(bb2);
                    break;
                case 3:
                    MyApp.outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }

          /*  if (isRtl)
            {
                printText(PrintUtils.UNICODE_TEXT);
            }*/



     /*       Arabic864 arabic = new Arabic864();
            byte[] arabicArr2 = arabic.Convert(str2, false);
            byte[] arabicArr1 = arabic.Convert(str1, false);

            MyApp.outputStream.write(isRtl?arabicArr2:arabicArr1);*/

            MyApp.outputStream.write(isRtl?str2.getBytes():str1.getBytes());
            //  MyApp.outputStream.write(PrinterCommands.LF);


            switch (size2) {
                case 0:
                    MyApp.outputStream.write(cc1);
                    break;
                case 1:
                    MyApp.outputStream.write(bb1);
                    break;
                case 2:
                    MyApp.outputStream.write(bb21);
                    break;
                case 3:
                    MyApp.outputStream.write(bb31);
                    break;
            }

            switch (align2) {
                case 0:
                    //left align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    MyApp.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }

          /*  if (str1.length() < 25) {
                int n = (31 - (str1.length() + str2.length()));
                str2 = new String(new char[n]).replace("\0", " ") + str2;
            }
                        MyApp.outputStream.write(str2.getBytes());

            */
            Log.d("PRINT_ERROR", "str2" + str2 + " length" + str2.length());

            writePrint((str1 + str2).length(), isRtl?str1:str2,isRtl);


            MyApp.outputStream.write(PrinterCommands.LF);


            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime[] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        return dateTime;
    }


    private void disconnect() {
        try {
            if (MyApp.outputStream != null) {
                MyApp.outputStream.close();
            }
            if (MyApp.btsocket != null) {
                MyApp.btsocket.close();
                MyApp.btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
