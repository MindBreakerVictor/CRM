package crm;

import crm.gui.*;
import crm.database.CRMDatabase;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            new MainWindow(new CRMDatabase());
        } catch (SQLException exception) {
            new ErrorWindow(exception.getMessage(), true);
        }
    }
}
