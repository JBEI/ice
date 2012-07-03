package org.jbei.ice.client.bulkimport.sheet;

/**
 * Factory class for determining what the input should be for a particular cell, based on the header field
 *
 * @author Hector Plahar
 */
public class SheetInputFactory {

    public static void switchToInput(final Header currentHeader, String text) {

    }
//        SuggestBox box;
//
//        // TODO : cache. this is called repeatedly for each click, resulting in the objects in here being created
//        switch (currentHeader) {
//            case BIOSAFETY:
//                MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
//                oracle.addAll(BioSafetyOptions.getDisplayList());
//                MultipleTextBox textBox = new MultipleTextBox();
//                textBox.setWidth("129px");
//                box = new SuggestBox(oracle, textBox);
//                box.setStyleName("cell_input");
////                box.setWidth("129px");
//                box.setText(text);
////                sheetTable.setWidget(currentRow, currentIndex, box);
//                textBox.setFocus(true);
//                break;
//
//            case SELECTION_MARKERS:
//                AutoCompleteField field = AutoCompleteField.fieldValue(currentHeader.name());
//                ArrayList<String> list = presenter.getAutoCompleteData(field);
//
//                oracle = new MultiWordSuggestOracle();
//                oracle.addAll(new TreeSet<String>(list));
//                textBox = new MultipleTextBox();
//                box = new SuggestBox(oracle, textBox);
//                box.setStyleName("cell_input");
////                box.setWidth("129px");
//                box.setText(text);
////                sheetTable.setWidget(currentRow, currentIndex, box);
//                textBox.setFocus(true);
//                break;
//
//            case ATT_FILENAME:
//            case SEQ_FILENAME:
//                CellUploader uploader = new CellUploader();
//                uploader.addOnFinishUploadHandler(new IUploader.OnFinishUploaderHandler() {
//                    @Override
//                    public void onFinish(IUploader uploader) {
//                        if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
//                            IUploader.UploadedInfo info = uploader.getServerInfo();
//                            if (info.message.isEmpty())
//                                return; // TODO : hook into error message
//
//                            // attachment or
//                            if (currentHeader == Header.ATT_FILENAME) {
//                                attachmentRowFileIds.put(currentRow, info.message);
//                            } else if (currentHeader == Header.SEQ_FILENAME) {
//                                sequenceRowFileIds.put(currentRow, info.message);
//                            }
//
//                            filename = info.name;
//                            selectCell(currentRow, currentIndex);
//                        } else {
//                            // TODO : notify user of error
//                        }
//                    }
//                });
//
//                sheetTable.setWidget(currentRow, currentIndex, uploader.asWidget());
//                break;
//
//            default:
//                input.setText(text);
//                sheetTable.setWidget(currentRow, currentIndex, input);
//                input.setFocus(true);
//        }
//
//        return box;
//    }
}
