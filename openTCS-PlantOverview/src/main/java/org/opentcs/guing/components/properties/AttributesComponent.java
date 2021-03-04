/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties;

import java.awt.Component;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JPanel;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.model.ComponentSelectionListener;
import org.opentcs.guing.model.ModelComponent;

/**
 * Eine Komponente zur Ansicht und Bearbeitung von Eigenschaftswerten.
 * Dargestellt werden die Eigenschaften in einer zweispaltigen Tabelle. Die
 * linke Spalte zeigt die Namen der Eigenschaften, die rechte die Werte. Durch
 * Anklicken eines Eigenschaftswerts kann dieser - je nach angemessener Form -
 * bearbeitet werden.
 * <p>
 * Die Komponente zeigt oberhalb der Tabelle einen Text
 * an, der das Objekt, zu dem die gerade angezeigten Eigenschaften gehören,
 * bezeichnet.
 * <p>
 * Wird eine Eigenschaft in der Tabelle angeklickt, so erscheint
 * im unteren Teil der Komponente ein Hilfetext, der die Eigenschaft genauer
 * erläutert.
 * <p>
 * Die Funktionen der Listener-Implementierungen: <br>
 * TableChangeListener: Hat der Benutzer in irgendeinem Tabellenfeld den
 * Editiermodus aktiviert, so ist davon auszugehen, dass sich dieses Attribut
 * geändert hat. Als registrierter TableChangeListener wird die
 * PropertiesComponent davon unterrichtet und kann daraufhin dem aktuellen
 * ModelComponent mitteilen, dass sich seine Attribute geändert haben. <br>
 * PropertiesModelChangeListener: PropertiesComponent stellt die Attribute
 * jeweils eines ModelComponent dar. Ändern sich die Attributwerte durch
 * irgendeinen Grund, so müssen die neuen Werte unverzüglich in der
 * PropertiesComponent zu sehen sein. Als registrierter
 * PropertiesModelChangeListener wird PropertiesComponent sofort von Änderungen
 * unterrichtet und kann sich aktualisieren. <br> ConnectionChangeListener:
 * Referenzen zwischen Knoten und Station laufen über eine allgemeine Referenz,
 * die alle Methodenaufrufe an eine spezialisierte Referenz delegiert. Die
 * spezialisierte Referenz richtet sich nach der Station. Wird nun die Referenz
 * von einer Station an eine andere gehängt, so ändert sich also die
 * spezialisierte Referenz und damit auch die darzustellenden Attribute. Da sich
 * die allgemeine Referenz jedoch dabei nicht ändert, schlägt die
 * Benachrichtigung der PropertiesComponent über den
 * PropertiesModelChangeListener fehl; der Weg über den ConnectionChangeListener
 * funktioniert aber.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AttributesComponent
    extends JPanel
    implements ComponentSelectionListener {

  /**
   * Die Tabelle mit den Properties.
   */
  private AttributesContent fPropertiesContent;
  private Component fPropertiesComponent;
  /**
   * Der Undo-Manager.
   */
  private final UndoRedoManager fUndoRedoManager;

  /**
   * Konstruktor mit Undo-Manager und Parent.
   *
   * @param undoManager
   */
  @Inject
  public AttributesComponent(UndoRedoManager undoManager) {
    fUndoRedoManager = requireNonNull(undoManager, "undoManager");
    initComponents();
  }

  @Override
  public void componentSelected(ModelComponent model) {
    setModel(model);
  }

  /**
   * Setzt eine neues ModelComponent-Objekt.
   *
   * @param model
   */
  public void setModel(ModelComponent model) {
    fPropertiesContent.setModel(model);
    fPropertiesComponent.setVisible(!model.getProperties().isEmpty());

    setDescription();
  }

  /**
   * Setzt die Anzeige zurück, wenn kein ModelComponente-Objekt mehr dargestellt
   * werden soll.
   */
  public void reset() {
    if (fPropertiesContent != null) {
      fPropertiesContent.reset();
    }

    descriptionLabel.setText("");
  }

  /**
   * Setzt den Text, der im oberen Teil der Komponente angezeigt wird. Der Text
   * bezeichnet das Objekt, dessen Eigenschaften in der Tabelle angezeigt
   * werden.
   */
  protected void setDescription() {
    descriptionLabel.setText(fPropertiesContent.getDescription());
  }

  /**
   * Setzt einen PropertiesContent an dem übergebenen Index.
   *
   * @param content
   */
  public void setPropertiesContent(AttributesContent content) {
    fPropertiesContent = content;
    fPropertiesContent.setup(fUndoRedoManager);
    fPropertiesComponent = add(content.getComponent());
    fPropertiesComponent.setVisible(false);
  }

  public AttributesContent getPropertiesContent() {
    return fPropertiesContent;
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JLabel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(descriptionLabel.getFont().getSize()+1f));
        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(descriptionLabel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel descriptionLabel;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}