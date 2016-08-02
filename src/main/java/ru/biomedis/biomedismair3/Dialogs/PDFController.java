package ru.biomedis.biomedismair3.Dialogs;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import ru.biomedis.biomedismair3.BaseController;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.biomedis.biomedismair3.Log.logger;

public class PDFController extends BaseController {

    @FXML private Pagination pagination ;
    @FXML private Label currentZoomLabel ;
    @FXML  private ScrollPane scroller ;
    @FXML private BorderPane root;
    @FXML private ImageView pdfimg;

    private FileChooser fileChooser ;
    private ObjectProperty<PDFFile> currentFile ;
    private ObjectProperty<ImageView> currentImage ;

    private DoubleProperty zoom ;
    private PageDimensions currentPageDimensions ;

    private ExecutorService imageLoadService ;

    private static final double ZOOM_DELTA = 1.05 ;

    private File docPath=null;


    // ************ Initialization *************

    public void initialize(URL location, ResourceBundle resources) {

        createAndConfigureImageLoadService();
       // createAndConfigureFileChooser();

        currentFile = new SimpleObjectProperty<>();
        //updateWindowTitleWhenFileChanges();

       // currentImage = new SimpleObjectProperty<>();
       // scroller.contentProperty().bind(currentImage);

        zoom = new SimpleDoubleProperty(1);
        // To implement zooming, we just get a new image from the PDFFile each time.
        // This seems to perform well in some basic tests but may need to be improved
        // E.g. load a larger image and scale in the ImageView, loading a new image only
        // when required.
        zoom.addListener((observable, oldValue, newValue) -> updateImage(pagination.getCurrentPageIndex(),false));
        currentZoomLabel.textProperty().bind(Bindings.format("%.0f %%", zoom.multiply(100)));
        bindPaginationToCurrentFile();
        createPaginationPageFactory();




    }

    private void createAndConfigureImageLoadService() {
        imageLoadService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    private void createAndConfigureFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        fileChooser.getExtensionFilters().add(new ExtensionFilter("PDF Files", "*.pdf", "*.PDF"));
    }

    private void updateWindowTitleWhenFileChanges() {
        currentFile.addListener((observable, oldFile, newFile) -> {
            try {
                String title = newFile == null ? "PDF Viewer" : newFile.getStringMetadata("Title");
                Window window1 = pagination.getScene().getWindow();
                if (window1 instanceof Stage) {
                    ((Stage) window1).setTitle(title);
                }
            } catch (IOException e) {
                logger.error("",e);
                showErrorMessage("Could not read title from pdf file", e);
            }
        });
    }

    private void bindPaginationToCurrentFile() {
        currentFile.addListener((observable, oldFile, newFile) -> {
            if (newFile != null) {
                pagination.setCurrentPageIndex(0);
                scroller.setVvalue(0);
                scroller.setHvalue(0);
            }
        });
        pagination.pageCountProperty().bind(new IntegerBinding() {
            {
                super.bind(currentFile);
            }
            @Override
            protected int computeValue() {
                return currentFile.get()==null ? 0 : currentFile.get().getNumPages() ;
            }
        });
        pagination.disableProperty().bind(Bindings.isNull(currentFile));
    }

    private void createPaginationPageFactory() {
        pagination.setPageFactory(pageNumber -> {
            if (currentFile.get() == null) {
                return null ;
            } else {
                if (pageNumber >= currentFile.get().getNumPages() || pageNumber < 0) {
                    return null ;
                } else {
                    updateImage(pageNumber,false);
                    scroller.setVvalue(0);
                    scroller.setHvalue(0);
                    return scroller ;
                }
            }
        });
    }

    // ************** Event Handlers ****************

     private void loadFile() {
        final File file = fileChooser.showOpenDialog(pagination.getScene().getWindow());
        if (file != null) {
            final Task<PDFFile> loadFileTask = new Task<PDFFile>() {
                @Override
                protected PDFFile call() throws Exception {
                    try (
                            RandomAccessFile raf = new RandomAccessFile(file, "r");
                            FileChannel channel = raf.getChannel()
                    ) {
                        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                        return new PDFFile(buffer);
                    }
                }
            };
            loadFileTask.setOnSucceeded(event -> {
                pagination.getScene().getRoot().setDisable(false);
                final PDFFile pdfFile = loadFileTask.getValue();

                currentFile.set(pdfFile);
            });
            loadFileTask.setOnFailed(event -> {
                pagination.getScene().getRoot().setDisable(false);
                showErrorMessage("Could not load file "+file.getName(), loadFileTask.getException());
            });
            pagination.getScene().getRoot().setDisable(true);
            imageLoadService.submit(loadFileTask);
        }
    }
    private void loadFile(File file) {

        if (file != null) {
            final Task<PDFFile> loadFileTask = new Task<PDFFile>() {
                @Override
                protected PDFFile call() throws Exception {
                    try (
                            RandomAccessFile raf = new RandomAccessFile(file, "r");
                            FileChannel channel = raf.getChannel()
                    ) {
                        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                        return new PDFFile(buffer);
                    }
                }
            };
            pagination.getScene().getWindow().widthProperty().addListener((observable, oldValue, newValue) -> resizeZoom());

            loadFileTask.setOnSucceeded(event -> {
                pagination.getScene().getRoot().setDisable(false);
                final PDFFile pdfFile = loadFileTask.getValue();
                currentFile.set(pdfFile);
                Platform.runLater(() -> resizeZoom());
            });
            loadFileTask.setOnFailed(event -> {
                pagination.getScene().getRoot().setDisable(false);
                showErrorMessage("Could not load file " + file.getName(), loadFileTask.getException());
            });

            pagination.getScene().getRoot().setDisable(true);
            imageLoadService.submit(loadFileTask);


        }
    }

    @FXML private void zoomIn() {
        zoom.set(zoom.get()*ZOOM_DELTA);
    }

    @FXML private void zoomOut() {
        zoom.set(zoom.get()/ZOOM_DELTA);
    }

    @FXML private void zoomFit() {
        // TODO: the -20 is a kludge to account for the width of the scrollbars, if showing.
        double horizZoom = (scroller.getWidth()-20) / currentPageDimensions.width ;
        double verticalZoom = (scroller.getHeight()-20) / currentPageDimensions.height ;
        zoom.set(Math.min(horizZoom, verticalZoom));
    }

    @FXML private void zoomWidth() {

        zoom.set((scroller.getWidth() - 20) / currentPageDimensions.width) ;
    }


    private void resizeZoom()
    {
        updateImage(pagination.getCurrentPageIndex(), true);

    }


    // *************** Background image loading ****************

    private void updateImage(final int pageNumber,boolean fit) {
        final Task<Image> updateImageTask = new Task<Image>()
        {
            @Override
            protected Image call() throws Exception {
                PDFPage page = currentFile.get().getPage(pageNumber+1);
                Rectangle2D bbox = page.getBBox();
                final double actualPageWidth = bbox.getWidth();
                final double actualPageHeight = bbox.getHeight();
                // record page dimensions for zoomToFit and zoomToWidth:
                currentPageDimensions = new PageDimensions(actualPageWidth, actualPageHeight);
                // width and height of image:
                final int width = (int) (actualPageWidth * zoom.get());
                final int height = (int) (actualPageHeight * zoom.get());
                // retrieve image for page:
                // width, height, clip, imageObserver, paintBackground, waitUntilLoaded:
                java.awt.Image awtImage = page.getImage(width, height, bbox, null, true, true);
                // draw image to buffered image:
                BufferedImage buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                buffImage.createGraphics().drawImage(awtImage, 0, 0, null);
                // convert to JavaFX image:
                Image image = SwingFXUtils.toFXImage(buffImage, null);
                // wrap in image view and return:
               // ImageView imageView = new ImageView(image);
               // imageView.setPreserveRatio(true);
                return image;
            }
        };

        updateImageTask.setOnSucceeded(event -> {
            pagination.getScene().getRoot().setDisable(false);
            //currentImage.set(updateImageTask.getValue());
            pdfimg.setImage(updateImageTask.getValue());
            if(fit) zoom.set((scroller.getWidth() - 20) / currentPageDimensions.width) ;

        });

        updateImageTask.setOnFailed(event -> {
            pagination.getScene().getRoot().setDisable(false);
            updateImageTask.getException().printStackTrace();
        });

        pagination.getScene().getRoot().setDisable(true);
        imageLoadService.submit(updateImageTask);

    }



    private void showErrorMessage(String message, Throwable exception) {

        // TODO: move to fxml (or better, use ControlsFX)

        final Stage dialog = new Stage();
        dialog.initOwner(pagination.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);
        final VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        StringWriter errorMessage = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorMessage));
        final Label detailsLabel = new Label(errorMessage.toString());
        TitledPane details = new TitledPane();
        details.setText("Details:");
        Label briefMessageLabel = new Label(message);
        final HBox detailsLabelHolder =new HBox();

        Button closeButton = new Button("OK");
        closeButton.setOnAction(event -> dialog.hide());
        HBox closeButtonHolder = new HBox();
        closeButtonHolder.getChildren().add(closeButton);
        closeButtonHolder.setAlignment(Pos.CENTER);
        closeButtonHolder.setPadding(new Insets(5));
        root.getChildren().addAll(briefMessageLabel, details, detailsLabelHolder, closeButtonHolder);
        details.setExpanded(false);
        details.setAnimated(false);

        details.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                detailsLabelHolder.getChildren().add(detailsLabel);
            } else {
                detailsLabelHolder.getChildren().remove(detailsLabel);
            }
            dialog.sizeToScene();
        });
        final Scene scene = new Scene(root);

        dialog.setScene(scene);
        dialog.show();
    }

    @Override
    public void setParams(Object... params)
    {
        System.out.println("setParams");
        if(params.length!=0)
        {
            docPath=(File)params[0];
        }


        //stage.setHeight(stage.getOwner().getHeight());
        //stage.setWidth(stage.getOwner().getWidth());


        File path=docPath;
        Platform.runLater(() -> loadFile(path));

    }




	/*
	 * Struct-like class intended to represent the physical dimensions of a page in pixels
	 * (as opposed to the dimensions of the (possibly zoomed) view.
	 * Used to compute zoom factors for zoomToFit and zoomToWidth.
	 *
	 */

    private class PageDimensions {
        private double width ;
        private double height ;
        PageDimensions(double width, double height) {
            this.width = width ;
            this.height = height ;
        }
        @Override
        public String toString() {
            return String.format("[%.1f, %.1f]", width, height);
        }
    }

}