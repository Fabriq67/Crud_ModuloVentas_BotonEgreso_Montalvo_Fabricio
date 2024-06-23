package end;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SalesFrame extends JFrame {
    private String clienteCodigo;
    private JComboBox<String> productComboBox;
    private JTextField quantityField;
    private JTextArea receiptArea;

    public SalesFrame(String clienteCodigo) {
        this.clienteCodigo = clienteCodigo;
        setTitle("Sales System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        JLabel productLabel = new JLabel("Seleccione Producto:");
        productComboBox = new JComboBox<>();

        JLabel quantityLabel = new JLabel("Cantidad:");
        quantityField = new JTextField();

        JButton addButton = new JButton("Agregar Producto");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });

        JButton confirmButton = new JButton("Confirmar Compra");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmPurchase();
            }
        });

        JButton approveButton = new JButton("Aprobar/Contabilizar");
        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                approveInvoice();
            }
        });

        JButton logicalDeleteButton = new JButton("Borrado Lógico");
        logicalDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logicalDelete();
            }
        });

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);

        panel.add(productLabel);
        panel.add(productComboBox);
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(addButton);
        panel.add(confirmButton);
        panel.add(approveButton);
        panel.add(logicalDeleteButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        loadClientInfo(clienteCodigo);
        loadProducts();
        setVisible(true);
    }

    private void loadClientInfo(String clienteCodigo) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT CLINOMBRE FROM CLIENTES WHERE CLICODIGO = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, clienteCodigo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String clientName = resultSet.getString("CLINOMBRE");
                receiptArea.append("Cliente: " + clientName + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar información del cliente");
        }
    }

    private void loadProducts() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT PROCODIGO, PRODESCRIPCION FROM PRODUCTOS WHERE PROSTATUS = 'ACT'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String productCode = resultSet.getString("PROCODIGO");
                String productDescription = resultSet.getString("PRODESCRIPCION");
                productComboBox.addItem(productCode + " - " + productDescription);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar productos");
        }
    }

    private void addProduct() {
        String selectedItem = (String) productComboBox.getSelectedItem();
        if (selectedItem == null || selectedItem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto válido");
            return;
        }

        String[] parts = selectedItem.split(" - ");
        String productCode = parts[0];
        String quantityStr = quantityField.getText();
        double quantity = Double.parseDouble(quantityStr);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT PRODESCRIPCION, PROPRECIOUM FROM PRODUCTOS WHERE PROCODIGO = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, productCode);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String description = resultSet.getString("PRODESCRIPCION");
                double price = resultSet.getDouble("PROPRECIOUM");
                double subtotal = quantity * price;
                receiptArea.append(productCode + " - " + description + " - " + quantity + " - " + subtotal + "\n");
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al agregar producto");
        }
    }

    private void confirmPurchase() {
        String selectedItem = (String) productComboBox.getSelectedItem();
        if (selectedItem == null || selectedItem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto válido");
            return;
        }

        String[] parts = selectedItem.split(" - ");
        String productCode = parts[0];
        String quantityStr = quantityField.getText();
        double quantity = Double.parseDouble(quantityStr);
        String facnumero = generateInvoiceNumber(); // Generar número de factura único

        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertFactura = "INSERT INTO FACTURAS (FACNUMERO, CLICODIGO, FACFECHA, FACSUBTOTAL, FACFORMAPAGO, FACSTATUS) VALUES (?, ?, current_date, ?, 'EFECTIVO', 'ABI')";
            PreparedStatement statementFactura = connection.prepareStatement(insertFactura);
            statementFactura.setString(1, facnumero);
            statementFactura.setString(2, clienteCodigo);
            statementFactura.setDouble(3, quantity * 2); // Ejemplo de subtotal
            statementFactura.executeUpdate();

            String insertDetail = "INSERT INTO PXF (FACNUMERO, PROCODIGO, PXFCANTIDAD, PXFVALOR, PXFSUBTOTAL, PXFSTATUS) VALUES (?, ?, ?, ?, ?, 'ABI')";
            PreparedStatement statementDetail = connection.prepareStatement(insertDetail);
            statementDetail.setString(1, facnumero);
            statementDetail.setString(2, productCode);
            statementDetail.setDouble(3, quantity);
            statementDetail.setDouble(4, 2); // Ejemplo de valor unitario
            statementDetail.setDouble(5, quantity * 2); // Ejemplo de subtotal
            statementDetail.executeUpdate();

            receiptArea.append("Factura creada con número: " + facnumero + "\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al confirmar la compra");
        }
    }

    private void approveInvoice() {
        String facnumero = JOptionPane.showInputDialog(this, "Ingrese el número de factura para aprobar:");

        if (facnumero == null || facnumero.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Número de factura no válido");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT aprobar_factura(?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, facnumero);
            statement.executeQuery();
            JOptionPane.showMessageDialog(this, "Factura aprobada y contabilizada con éxito");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al aprobar la factura");
        }
    }

    private void logicalDelete() {
        String facnumero = JOptionPane.showInputDialog(this, "Ingrese el número de factura para borrar lógicamente:");

        if (facnumero == null || facnumero.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Número de factura no válido");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateFactura = "UPDATE FACTURAS SET FACSTATUS = 'INA' WHERE FACNUMERO = ?";
            PreparedStatement statementFactura = connection.prepareStatement(updateFactura);
            statementFactura.setString(1, facnumero);
            statementFactura.executeUpdate();

            String updateDetail = "UPDATE PXF SET PXFSTATUS = 'INA' WHERE FACNUMERO = ?";
            PreparedStatement statementDetail = connection.prepareStatement(updateDetail);
            statementDetail.setString(1, facnumero);
            statementDetail.executeUpdate();

            receiptArea.append("Factura con número " + facnumero + " borrada lógicamente.\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al borrar lógicamente la factura");
        }
    }

    private String generateInvoiceNumber() {
        long currentTimeMillis = System.currentTimeMillis();
        return String.format("%09d", currentTimeMillis % 1000000000);
    }
}
