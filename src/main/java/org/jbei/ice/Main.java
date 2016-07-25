package org.jbei.ice;

import com.opencsv.CSVWriter;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Entry;

import java.io.File;
import java.io.FileWriter;

/**
 * @author Hector Plahar
 */
public class Main {

    public static void main(String[] args) throws Exception {

        String[] names = new String[]{"5B_5B_06135_Cellvibr", "5B_5B_05482_Cellvibr", "6D_6D_17171_Cellvibr", "6D_6D_16864_Cellvibr", "4A_4A_53159_Cellulom", "5B_5B_38662_Cellulom", "6D_6D_104457_Bactero", "6D_6D_21960_Xanthomo", "5B_5B_27976_Actinopl", "GH12", "4A_4A_31450_Cellvibr", "6D_6D_10061_Cellvibr", "6D_6D_16739_Cellvibr", "6D_6D_14448_Cellvibr", "4A_4A_49503_Unknown_", "5B_5B_00758_Cellvibr", "5B_5B_01848_Cellvibr", "4A_4A_30784_Rhodanob", "5B_5B_08141_Cellvibr", "6D_6D_19007_Teredini", "5B_5B_00395_Weeksell", "4A_4A_21737_Pseudoxa", "6D_6D_14958_Cellvibr", "5B_5B_07613_Pseudomo", "6D_6D_19132_Cellvibr", "4A_4A_53160_Cellulom", "5B_5B_51088_Cellulom", "5B_5B_40375_Cell_v1", "5B_5B_40375_Cell_v2", "5B_5B_08163_Cellvibr", "6D_6D_17432_Simiduia", "4A_4A_42780_Cellulom", "4A_4A_37290_Cellulom", "4A_4A_19271_Cellulom", "5B_5B_01656_Cellvibr", "6D_6D_14450_Cellvibr", "GH43", "5B_5B_01657_Cellvibr", "5B_5B_11231_Shewanel", "5B_5B_44172_Cellulom", "5B_5B_42935_Cellulom", "6D_6D_61425_Sideroxy", "CE8", "5B_5B_06415_Cellvibr", "6D_6D_19123_Cellvibr", "6D_6D_37592_Unknown", "5B_5B_46968_Cellulom", "5B_5B_02458_Cellvibr", "5B_5B_00652_Cellvibr", "6D_6D_19419_Cellvibr", "4A_4A_19121_Pseudoxa", "5B_5B_41331_Cellulom", "4A_4A_18207_Xanthomo", "5B_5B_01628_Cellvibr", "5B_5B_06677_Cellvibr", "5B_5B_38447_Cellulom", "4A_4A_20550_Achromob", "4A_4A_18145_Cellulom", "5B_5B_08446_Cellvibr", "5B_5B_06722_Cellvibr", "6D_6D_13778_Cellvibr", "6D_6D_20654_Cellvibr", "5B_5B_45244_Cellulom", "6D_6D_30062_Escheric", "5B_5B_02227_Cellvibr", "4A_4A_17030_Rhodanob", "5B_5B_06192_Cellvibr", "6D_6D_14607_Saccharo", "5B_5B_06424_Cellvibr", "4A_4A_48968_Cellulom", "6D_6D_12863_Acidovor", "5B_5B_07615_Pseudomo", "4A_4A_14338_Cellulom", "6D_6D_16054_Methylov", "6D_6D_10058_Cellvibr", "5B_5B_02461_Cellvibr", "GH43_1", "5B_5B_01512_Cellvibr", "5B_5B_08261_Methylom", "4A_4A_42983_Cellulom", "5B_5B_27977_Actinopl", "5B_5B_38663_Cellulom", "5B_5B_22189_Xylanimo", "6D_6D_23205_Cellvibr", "5B_5B_43054_Streptom", "4A_4A_17769_Cellulom", "5B_5B_01513_Cellvibr", "5B_5B_07328_Cellvibr", "GH76", "CBM13_CBM13_GH62", "GH43_2", "6D_6D_14326_Bacillus", "5B_5B_08484_Cellvibr", "5B_5B_05341_Cellvibr", "6D_6D_11430_Cellvibr", "4A_4A_44697_Cellulom", "6D_6D_14610_Saccharo", "5B_5B_06133_Cellvibr", "6D_6D_16742_Cellvibr", "4A_4A_26223_Cellulom", "4A_4A_25969_Clavibac", "4A_4A_27881_Rhodanob", "4A_4A_19667_Isopteri", "5B_5B_24819_Isopteri", "5B_5B_01625_Cellvibr", "4A_4A_26856_Pseudoxa", "GH93_CBM13", "6D_6D_12864_Acidovor", "5B_5B_06101_Cellvibr", "4A_4A_35559_Cellulom", "GH54_CBM13_CBM13", "5B_5B_05484_Cellvibr", "5B_5B_01521_Cellvibr", "5B_5B_37401_Cellulom", "PL11", "5B_5B_22218_Cellulom", "6D_6D_37400_Unknown_", "4A_4A_37414_Pseudoxa", "5B_5B_01784_Cellvibr", "5B_5B_06428_Cellvibr", "6D_6D_19126_Cellvibr", "4A_4A_19663_Isopteri", "6D_6D_30061_Escheric", "6D_6D_13788_Cellvibr", "5B_5B_06731_Cellvibr", "5B_5B_06097_Cellvibr", "5B_5B_06418_Cellvibr", "GH28", "4A_4A_18610_Cellulom", "5B_5B_30602_Nocardio", "4A_4A_33814_Pseudoxa", "5B_5B_02661_Sphingob", "5B_5B_02213_Cellvibr", "6D_6D_28672_Cellvibr", "4A_4A_19443_Cellulom", "4A_4A_24267_Cellulom", "5B_5B_06419_Cellvibr", "5B_5B_07237_Cellvibr", "4A_4A_18146_Cellulom", "4A_4A_13277_Cellulom", "5B_5B_02457_Cellvibr", "6D_6D_19127_Cellvibr", "4A_4A_25860_Rhodanob", "4A_4A_24228_Cellulom", "6D_6D_10062_Cellvibr", "5B_5B_18249_Cellulom"};
        System.out.println(names.length);

        File file = new File("data.csv");

        if (file.exists() && !file.delete())
        {
            System.out.println("could not delete file " + file.toPath().toString());
            return;
        }

        CSVWriter writer = new CSVWriter(new FileWriter(file));

        HibernateUtil.beginTransaction();
        for (String name : names) {
            try {
                Entry entry = DAOFactory.getEntryDAO().getByPartNumber(name);
                if (entry == null) {
                    writer.writeNext(new String[]{name, "NOT FOUND"});
                    continue;
                }

                Entry linked = DAOFactory.getEntryDAO().getParents(entry.getId()).get(0);
                writer.writeNext(new String[]{entry.getName(), linked.getPartNumber()});
            } catch (Exception e) {
                writer.writeNext(new String[]{name, "ERROR"});
                continue;
            }
        }
        HibernateUtil.commitTransaction();
        writer.close();
    }
}
