
package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.Border;

public class ListaTareas2 extends javax.swing.JFrame {

    private List<Object[]> todasLasTareas = new ArrayList<>(); // Lista para almacenar todas las tareas
    private String usuarioSeleccionado = "Todos"; // Declaración global
    private List<String> listaUsuarios = new ArrayList<>(); // Lista para almacenar usuarios únicos

    public ListaTareas2() {
        setUndecorated(true);
        initComponents();
        cargarTareas();
        configurarFiltros();
        configurarColumnas();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void cargarTareas() {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Nombre");
        modelo.addColumn("Descripción");
        modelo.addColumn("Prioridad");
        modelo.addColumn("Estado");
        modelo.addColumn("Usuario");
        modelo.addColumn("Fecha Inicio");
        modelo.addColumn("Fecha Fin");

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        todasLasTareas.clear();
        listaUsuarios.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader("tareas.txt"))) {
            String linea;
            String nombre = "";
            String descripcion = "";
            String prioridad = "";
            String estado = "";
            String usuario = "Sin designar";
            String fechaInicio = "";
            String fechaFin = "";

            while ((linea = reader.readLine()) != null) {
                if (linea.equals("---------------------------")) {
                    Object[] tarea = {nombre, descripcion, prioridad, estado, usuario, fechaInicio, fechaFin};
                    todasLasTareas.add(tarea);
                    modelo.addRow(tarea);

                    if (!listaUsuarios.contains(usuario) && !usuario.equals("Sin designar")) {
                        listaUsuarios.add(usuario);
                    }

                    nombre = "";
                    descripcion = "";
                    prioridad = "";
                    estado = "";
                    usuario = "Sin designar";
                    fechaInicio = "";
                    fechaFin = "";
                } else if (linea.startsWith("Nombre: ")) {
                    nombre = linea.substring(8);
                } else if (linea.startsWith("Descripción: ")) {
                    descripcion = linea.substring(13);
                } else if (linea.startsWith("Prioridad: ")) {
                    prioridad = linea.substring(11);
                } else if (linea.startsWith("Estado: ")) {
                    estado = linea.substring(8);
                } else if (linea.startsWith("Usuario: ")) {
                    usuario = linea.substring(9);
                } else if (linea.startsWith("Fecha Inicio: ")) {
                    fechaInicio = linea.substring(14);
                    try {
                        LocalDate dateInicio = LocalDate.parse(fechaInicio, inputFormatter);
                        fechaInicio = dateInicio.format(outputFormatter);
                    } catch (DateTimeParseException e) {
                        fechaInicio = "Fecha inválida";
                    }
                } else if (linea.startsWith("Fecha Fin: ")) {
                    fechaFin = linea.substring(11);
                    try {
                        LocalDate dateFin = LocalDate.parse(fechaFin, inputFormatter);
                        fechaFin = dateFin.format(outputFormatter);
                    } catch (DateTimeParseException e) {
                        fechaFin = "Fecha inválida";
                    }
                }
            }
        } catch (IOException e) {
            modelo.addRow(new Object[]{"Error", "No se pudieron cargar las tareas", "", "", "", "", e.getMessage()});
        }

        actualizarUsuarios();
        tablaTareas.setModel(modelo);
        aplicarColores();
    }

    private void aplicarColores() {
        tablaTareas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 2) { // Prioridad
                    String prioridad = (String) table.getValueAt(row, 2);
                    if ("Baja".equals(prioridad)) {
                        c.setBackground(Color.GREEN);
                        c.setForeground(Color.BLACK);
                    } else if ("Media".equals(prioridad)) {
                        c.setBackground(Color.YELLOW);
                        c.setForeground(Color.BLACK);
                    } else if ("Alta".equals(prioridad)) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else if (column == 3) { // Estado
                    String estado = (String) table.getValueAt(row, 3);
                    if ("Completada".equals(estado)) {
                        c.setBackground(Color.GREEN);
                        c.setForeground(Color.BLACK);
                    } else if ("En Progreso".equals(estado)) {
                        c.setBackground(Color.YELLOW);
                        c.setForeground(Color.BLACK);
                    } else if ("Por Hacer".equals(estado)) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }

                return c;
            }
        });
    }

    private void actualizarUsuarios() {
        usuarioFiltro.removeAllItems();
        usuarioFiltro.addItem("Todos");
        for (String usuario : listaUsuarios) {
            usuarioFiltro.addItem(usuario);
        }
    }

    private void configurarFiltros() {
        prioridad.addActionListener(e -> aplicarFiltros());
        estado.addActionListener(e -> aplicarFiltros());
        usuarioFiltro.addActionListener(e -> {
            usuarioSeleccionado = (String) usuarioFiltro.getSelectedItem();
            aplicarFiltros();
        });
        descripcionFiltro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                aplicarFiltros();
            }
        });
    }

    private void aplicarFiltros() {
        String estadoSeleccionado = (String) estado.getSelectedItem();
        String prioridadSeleccionada = (String) prioridad.getSelectedItem();
        String descripcionFiltroTexto = descripcionFiltro.getText().toLowerCase();

        DefaultTableModel modelo = (DefaultTableModel) tablaTareas.getModel();
        modelo.setRowCount(0);

        for (Object[] tarea : todasLasTareas) {
            String estadoTarea = (String) tarea[3];
            String prioridadTarea = (String) tarea[2];
            String usuarioTarea = (String) tarea[4];
            String descripcionTarea = ((String) tarea[1]).toLowerCase();
            String nombreTarea = ((String) tarea[0]).toLowerCase();

            boolean cumpleEstado = estadoSeleccionado.equals("Todos") || estadoTarea.equals(estadoSeleccionado);
            boolean cumplePrioridad = prioridadSeleccionada.equals("Todos") || prioridadTarea.equals(prioridadSeleccionada);
            boolean cumpleUsuario = usuarioSeleccionado.equals("Todos") || usuarioTarea.equals(usuarioSeleccionado);
            boolean cumpleDescripcion = descripcionFiltroTexto.isEmpty() || descripcionTarea.contains(descripcionFiltroTexto) || nombreTarea.contains(descripcionFiltroTexto);

            if (cumpleEstado && cumplePrioridad && cumpleUsuario && cumpleDescripcion) {
                modelo.addRow(tarea);
            }
        }
    }



   private void configurarColumnas() {
    TableColumn nombreColumna = tablaTareas.getColumnModel().getColumn(0);
    nombreColumna.setPreferredWidth(230);

    TableColumn descripcionColumna = tablaTareas.getColumnModel().getColumn(1);
    descripcionColumna.setPreferredWidth(300);
    descripcionColumna.setCellRenderer(new MultiLineRenderer());

    TableColumn prioridadColumna = tablaTareas.getColumnModel().getColumn(2);
    prioridadColumna.setPreferredWidth(140);

    TableColumn estadoColumna = tablaTareas.getColumnModel().getColumn(3);
    estadoColumna.setPreferredWidth(140);

    TableColumn usuarioColumna = tablaTareas.getColumnModel().getColumn(4);
    usuarioColumna.setPreferredWidth(140);

    TableColumn fechaInicioColumna = tablaTareas.getColumnModel().getColumn(5);
    fechaInicioColumna.setPreferredWidth(130);

    TableColumn fechaFinColumna = tablaTareas.getColumnModel().getColumn(6);
    fechaFinColumna.setPreferredWidth(130);

    ajustarAlturaFilas(); // Ajustar la altura de las filas dinámicamente
}

// Clase para ajustar el texto en varias líneas
class MultiLineRenderer extends JTextArea implements TableCellRenderer {
    public MultiLineRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value != null ? value.toString() : "");
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);

        int requiredHeight = getPreferredSize().height;
        if (table.getRowHeight(row) < requiredHeight) {
            table.setRowHeight(row, requiredHeight); // Ajustar la altura de la fila
        }

        return this;
    }
}

private void ajustarAlturaFilas() {
    for (int row = 0; row < tablaTareas.getRowCount(); row++) {
        int maxHeight = 0;

        for (int column = 0; column < tablaTareas.getColumnCount(); column++) {
            TableCellRenderer renderer = tablaTareas.getCellRenderer(row, column);
            Component comp = tablaTareas.prepareRenderer(renderer, row, column);
            int height = comp.getPreferredSize().height;
            maxHeight = Math.max(maxHeight, height);
        }

        if (tablaTareas.getRowHeight(row) != maxHeight) {
            tablaTareas.setRowHeight(row, maxHeight); // Establecer altura máxima encontrada
        }
    }
}





    @SuppressWarnings("unchecked")
    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblCierre = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        crearTarea = new javax.swing.JLabel();
        imagen1 = new modelo.Imagen();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaTareas = new javax.swing.JTable();
        prioridad = new javax.swing.JComboBox<>();
        estado = new javax.swing.JComboBox<>();
        crearTarea1 = new javax.swing.JLabel();
        crearTarea2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        usuarioFiltro = new javax.swing.JComboBox<>();
        descripcionFiltro = new javax.swing.JTextField();
        crearTarea3 = new javax.swing.JLabel();
        imagen2 = new modelo.Imagen();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 236, 209));
        jPanel1.setPreferredSize(new java.awt.Dimension(500, 330));
        jPanel1.setLayout(null);

        lblCierre.setFont(new java.awt.Font("Montserrat", 1, 24)); // NOI18N
        lblCierre.setForeground(new java.awt.Color(255, 0, 0));
        lblCierre.setText("O");
        lblCierre.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblCierre.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCierreMouseClicked(evt);
            }
        });
        jPanel1.add(lblCierre);
        lblCierre.setBounds(700, 20, 20, 20);

        jLabel2.setFont(new java.awt.Font("Montserrat", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 102, 0));
        jLabel2.setText("O");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(680, 20, 20, 20);

        jLabel1.setFont(new java.awt.Font("Montserrat", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 255, 0));
        jLabel1.setText("O");
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(jLabel1);
        jLabel1.setBounds(660, 20, 20, 20);

        crearTarea.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        crearTarea.setForeground(new java.awt.Color(0, 153, 153));
        crearTarea.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        crearTarea.setText("Estado");
        jPanel1.add(crearTarea);
        crearTarea.setBounds(140, 430, 70, 20);

        imagen1.setText("imagen1");
        imagen1.setRuta("/resources/listar.png");
        jPanel1.add(imagen1);
        imagen1.setBounds(10, 0, 70, 70);

        tablaTareas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7"
            }
        ));
        tablaTareas.setRowHeight(30);
        tablaTareas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tablaTareasMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tablaTareasMouseExited(evt);
            }
        });
        jScrollPane2.setViewportView(tablaTareas);
        if (tablaTareas.getColumnModel().getColumnCount() > 0) {
            tablaTareas.getColumnModel().getColumn(0).setHeaderValue("Title 1");
            tablaTareas.getColumnModel().getColumn(1).setPreferredWidth(200);
            tablaTareas.getColumnModel().getColumn(1).setHeaderValue("Title 2");
            tablaTareas.getColumnModel().getColumn(2).setHeaderValue("Title 3");
            tablaTareas.getColumnModel().getColumn(3).setHeaderValue("Title 4");
            tablaTareas.getColumnModel().getColumn(4).setHeaderValue("Title 5");
            tablaTareas.getColumnModel().getColumn(5).setHeaderValue("Title 6");
            tablaTareas.getColumnModel().getColumn(6).setHeaderValue("Title 7");
        }

        jPanel1.add(jScrollPane2);
        jScrollPane2.setBounds(40, 80, 680, 330);

        prioridad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Alta", "Media", "Baja" }));
        prioridad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prioridadActionPerformed(evt);
            }
        });
        jPanel1.add(prioridad);
        prioridad.setBounds(70, 430, 72, 22);

        estado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Completada", "En Progreso", "Por Hacer", " ", " " }));
        jPanel1.add(estado);
        estado.setBounds(210, 430, 90, 22);

        crearTarea1.setFont(new java.awt.Font("Cooper Black", 1, 36)); // NOI18N
        crearTarea1.setForeground(new java.awt.Color(0, 153, 153));
        crearTarea1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        crearTarea1.setText("LISTA DE TAREAS");
        jPanel1.add(crearTarea1);
        crearTarea1.setBounds(180, 20, 420, 50);

        crearTarea2.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        crearTarea2.setForeground(new java.awt.Color(0, 153, 153));
        crearTarea2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        crearTarea2.setText("Prioridad");
        jPanel1.add(crearTarea2);
        crearTarea2.setBounds(0, 430, 70, 20);

        jPanel4.setBackground(new java.awt.Color(0, 51, 102));

        jLabel4.setFont(new java.awt.Font("Montserrat", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Crear Tarea");
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel4MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel4MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel4MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanel4);
        jPanel4.setBounds(620, 430, 120, 30);

        usuarioFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(usuarioFiltro);
        usuarioFiltro.setBounds(370, 430, 72, 22);

        descripcionFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descripcionFiltroActionPerformed(evt);
            }
        });
        jPanel1.add(descripcionFiltro);
        descripcionFiltro.setBounds(490, 430, 120, 22);

        crearTarea3.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        crearTarea3.setForeground(new java.awt.Color(0, 153, 153));
        crearTarea3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        crearTarea3.setText("Usuario");
        jPanel1.add(crearTarea3);
        crearTarea3.setBounds(300, 430, 70, 20);

        imagen2.setText("imagen1");
        imagen2.setRuta("/resources/Lupa.png");
        jPanel1.add(imagen2);
        imagen2.setBounds(450, 430, 46, 40);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblCierreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCierreMouseClicked
     Menu2 menu=new Menu2();
      menu.setVisible(true);
      dispose();
    }//GEN-LAST:event_lblCierreMouseClicked

    private void tablaTareasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTareasMouseEntered
    Border borde = BorderFactory.createLineBorder(new Color(52,136,235),2);
    tablaTareas.setBorder(borde);
    }//GEN-LAST:event_tablaTareasMouseEntered

    private void tablaTareasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTareasMouseExited
    tablaTareas.setBorder(null);
    }//GEN-LAST:event_tablaTareasMouseExited

    private void prioridadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prioridadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_prioridadActionPerformed

    private void jLabel4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseEntered
        Border borde = BorderFactory.createLineBorder(new Color(52,136,235),2);
        jLabel4.setBorder(borde);
    }//GEN-LAST:event_jLabel4MouseEntered

    private void jLabel4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseExited
        jLabel4.setBorder(null);
    }//GEN-LAST:event_jLabel4MouseExited

    private void jLabel4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MousePressed
        CrearTarea2 crearTarea=new CrearTarea2();
       crearTarea.setVisible(true);
       dispose();
    }//GEN-LAST:event_jLabel4MousePressed

    private void descripcionFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descripcionFiltroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_descripcionFiltroActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel crearTarea;
    private javax.swing.JLabel crearTarea1;
    private javax.swing.JLabel crearTarea2;
    private javax.swing.JLabel crearTarea3;
    private javax.swing.JTextField descripcionFiltro;
    private javax.swing.JComboBox<String> estado;
    private modelo.Imagen imagen1;
    private modelo.Imagen imagen2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCierre;
    private javax.swing.JComboBox<String> prioridad;
    private javax.swing.JTable tablaTareas;
    private javax.swing.JComboBox<String> usuarioFiltro;
    // End of variables declaration//GEN-END:variables
}
