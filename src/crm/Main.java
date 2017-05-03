package crm;

import crm.database.CRMDatabase;
import crm.gui.*;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try (CRMDatabase database = new CRMDatabase()) {
            MainWindow mainWindow = new MainWindow(database);
        } catch (SQLException exception) {
            ErrorWindow errorWindow = new ErrorWindow(exception.getMessage(), true);
        }
    }
}
