package crm;

import crm.gui.*;
import crm.database.CRMDatabase;
import crm.database.CRMDBNotConnectedException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            new MainWindow(new CRMDatabase());
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database not connected.");
        } catch (SQLException exception) {
            new ErrorWindow(exception.getMessage(), true);
        }
    }
}
