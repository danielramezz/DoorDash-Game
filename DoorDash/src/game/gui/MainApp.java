package game.gui;

import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.scene.effect.BoxBlur;
import javafx.scene.shape.Circle;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.effect.BoxBlur;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.text.TextAlignment;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Group;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Group;

public class MainApp extends Application {

    private static final int APP_WIDTH = 1366;
    private static final int APP_HEIGHT = 768;

    private Stage primaryStage;
    private AudioManager audioManager;
    private StackPane appViewport;
    private Group appScaledContent;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        audioManager = new AudioManager();

        primaryStage.setTitle("DooR DasH: Scare vs Laugh Touchdown");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);

        showStartScreen();
        primaryStage.show();
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.setFullScreenExitHint("");

        primaryStage.setOnCloseRequest(e -> {
            audioManager.stopAll();
            primaryStage.close();
        });
    }
    private void setScene(Parent content) {
        if (appViewport == null) {
            appViewport = new StackPane();
            appViewport.setStyle("-fx-background-color: black;");

            ImageView outerBackground = new ImageView();
            
            outerBackground.setPreserveRatio(false);
            outerBackground.setSmooth(true);
            outerBackground.setOpacity(0.55);
            outerBackground.fitWidthProperty().bind(appViewport.widthProperty());
            outerBackground.fitHeightProperty().bind(appViewport.heightProperty());

            try {
                URL bgUrl = getClass().getResource("/assets/images/start_background.png");
                if (bgUrl != null) {
                    outerBackground.setImage(new Image(bgUrl.toExternalForm()));
                }
            } catch (Exception e) {
                System.out.println("Could not load outer fullscreen background.");
            }

            Rectangle outerDark = new Rectangle();
            outerDark.widthProperty().bind(appViewport.widthProperty());
            outerDark.heightProperty().bind(appViewport.heightProperty());
            outerDark.setFill(Color.rgb(0, 0, 0, 0.42));
            outerDark.setMouseTransparent(true);

            appScaledContent = new Group();

            NumberBinding scale = Bindings.min(
                    appViewport.widthProperty().divide(APP_WIDTH),
                    appViewport.heightProperty().divide(APP_HEIGHT)
            );

            appScaledContent.scaleXProperty().bind(scale);
            appScaledContent.scaleYProperty().bind(scale);

            appViewport.getChildren().addAll(outerBackground, outerDark, appScaledContent);
            StackPane.setAlignment(appScaledContent, Pos.CENTER);

            mainScene = new Scene(appViewport, APP_WIDTH, APP_HEIGHT, Color.BLACK);
            primaryStage.setScene(mainScene);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        }

        if (content instanceof Region) {
            Region region = (Region) content;
            region.setPrefSize(APP_WIDTH, APP_HEIGHT);
            region.setMinSize(APP_WIDTH, APP_HEIGHT);
            region.setMaxSize(APP_WIDTH, APP_HEIGHT);
        }

        appScaledContent.getChildren().setAll(content);
    }
    private void toggleFullScreen() {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
    }
    private Scene mainScene;
    

    private void showStartScreen() {
        audioManager.stopMusic();

        StartScreenView view = new StartScreenView(
                audioManager,
                this::showGameScreen,
                this::showInstructionsScreen,
                this::toggleFullScreen,
                () -> {
                    audioManager.stopAll();
                    primaryStage.close();
                }
        );

        setScene(view.getRoot());
        audioManager.playMenuMusic();
    }
    private final String[] loadingTips = {
            "Charging the scare grid...",
            "Routing conveyor belts...",
            "Powering Boo's Door...",
            "Calibrating monster abilities...",
            "Warming up the factory floor..."
    };

    private void showInstructionsScreen() {
        setScene(new InstructionsView(this::showStartScreen).getRoot());
    }

    private void showGameScreen(GameConfig config) {
        showLoadingScreen(config);
    }

    public static void main(String[] args) {
        launch(args);
    }
    private void openRealGameScreen(GameConfig config) {
        audioManager.playGameplayMusic();
        setScene(new GameScreenView(audioManager, config, this::showStartScreen).getRoot());
    }
    private void showLoadingScreen(GameConfig config) {
        audioManager.stopMusic();

        StackPane root = new StackPane();
        root.setPrefSize(APP_WIDTH, APP_HEIGHT);
        root.setStyle("-fx-background-color: black;");

        Image bgImage = null;

        try {
            URL bgUrl = getClass().getResource("/assets/images/loading_screen.png");

            if (bgUrl != null) {
                bgImage = new Image(bgUrl.toExternalForm());
            } else {
                System.out.println("Missing image: /assets/images/loading_screen.png");
            }
        } catch (Exception e) {
            System.out.println("Could not load loading screen image.");
        }

        if (bgImage != null) {
            ImageView bg = new ImageView(bgImage);
            bg.setFitWidth(APP_WIDTH);
            bg.setFitHeight(APP_HEIGHT);
            bg.setPreserveRatio(false);
            bg.setSmooth(true);

            bg.setScaleX(1.02);
            bg.setScaleY(1.02);

            ScaleTransition bgZoom = new ScaleTransition(Duration.seconds(3.2), bg);
            bgZoom.setFromX(1.02);
            bgZoom.setFromY(1.02);
            bgZoom.setToX(1.08);
            bgZoom.setToY(1.08);
            bgZoom.setInterpolator(Interpolator.EASE_BOTH);
            bgZoom.play();

            TranslateTransition bgPan = new TranslateTransition(Duration.seconds(3.2), bg);
            bgPan.setFromX(-10);
            bgPan.setToX(10);
            bgPan.setInterpolator(Interpolator.EASE_BOTH);
            bgPan.play();

            root.getChildren().add(bg);
        } else {
            root.getChildren().add(CinematicBackground.createRoot());
        }

        Rectangle darkOverlay = new Rectangle(APP_WIDTH, APP_HEIGHT);
        darkOverlay.setFill(Color.rgb(0, 0, 0, 0.28));

        Rectangle bottomShade = new Rectangle(APP_WIDTH, APP_HEIGHT);
        bottomShade.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.rgb(0, 0, 0, 0.00)),
                new Stop(0.55, Color.rgb(0, 0, 0, 0.15)),
                new Stop(1.00, Color.rgb(0, 0, 0, 0.70))
        ));

        root.getChildren().addAll(darkOverlay, bottomShade);

        VBox content = new VBox(14);
        content.setAlignment(Pos.BOTTOM_CENTER);
        content.setPadding(new Insets(0, 0, 42, 0));
        StackPane.setAlignment(content, Pos.BOTTOM_CENTER);

        Label loadingTitle = new Label("Loading Factory Floor...");
        loadingTitle.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 30));
        loadingTitle.setTextFill(Color.WHITE);

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.rgb(85, 235, 255, 0.85));
        titleGlow.setRadius(24);
        titleGlow.setSpread(0.22);
        loadingTitle.setEffect(titleGlow);

        Label tipLabel = new Label("Charging the scare grid...");
        tipLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        tipLabel.setTextFill(Color.rgb(220, 232, 245));

        StackPane barOuter = new StackPane();
        barOuter.setPrefSize(620, 38);
        barOuter.setMaxSize(620, 38);

        Rectangle shell = new Rectangle(620, 38);
        shell.setArcWidth(28);
        shell.setArcHeight(28);
        shell.setFill(Color.rgb(5, 10, 24, 0.88));
        shell.setStroke(Color.rgb(90, 235, 255, 0.80));
        shell.setStrokeWidth(2);

        DropShadow shellGlow = new DropShadow();
        shellGlow.setColor(Color.rgb(75, 225, 255, 0.55));
        shellGlow.setRadius(22);
        shellGlow.setSpread(0.18);
        shell.setEffect(shellGlow);

        Rectangle fill = new Rectangle(0, 24);
        fill.setArcWidth(20);
        fill.setArcHeight(20);
        fill.setFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.rgb(65, 175, 255)),
                new Stop(0.48, Color.rgb(85, 255, 220)),
                new Stop(1.00, Color.rgb(160, 255, 110))
        ));

        DropShadow fillGlow = new DropShadow();
        fillGlow.setColor(Color.rgb(100, 255, 220, 0.90));
        fillGlow.setRadius(18);
        fillGlow.setSpread(0.24);
        fill.setEffect(fillGlow);

        StackPane fillHolder = new StackPane(fill);
        fillHolder.setAlignment(Pos.CENTER_LEFT);
        fillHolder.setPadding(new Insets(0, 0, 0, 8));

        Rectangle fillClip = new Rectangle(604, 24);
        fillClip.setArcWidth(20);
        fillClip.setArcHeight(20);
        fillHolder.setClip(fillClip);

        Rectangle shimmer = new Rectangle(100, 24);
        shimmer.setArcWidth(20);
        shimmer.setArcHeight(20);
        shimmer.setFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.rgb(255, 255, 255, 0.00)),
                new Stop(0.50, Color.rgb(255, 255, 255, 0.42)),
                new Stop(1.00, Color.rgb(255, 255, 255, 0.00))
        ));
        shimmer.setTranslateX(-310);

        StackPane shimmerHolder = new StackPane(shimmer);
        shimmerHolder.setAlignment(Pos.CENTER_LEFT);
        shimmerHolder.setPadding(new Insets(0, 0, 0, 8));

        Rectangle shimmerClip = new Rectangle(604, 24);
        shimmerClip.setArcWidth(20);
        shimmerClip.setArcHeight(20);
        shimmerHolder.setClip(shimmerClip);

        Timeline shimmerAnim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(shimmer.translateXProperty(), -310)),
                new KeyFrame(Duration.seconds(1.0), new KeyValue(shimmer.translateXProperty(), 310))
        );
        shimmerAnim.setCycleCount(Animation.INDEFINITE);
        shimmerAnim.play();

        Label percentLabel = new Label("0%");
        percentLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 16));
        percentLabel.setTextFill(Color.WHITE);

        barOuter.getChildren().addAll(shell, fillHolder, shimmerHolder, percentLabel);

        content.getChildren().addAll(loadingTitle, tipLabel, barOuter);
        root.getChildren().add(content);

        setScene(root);

        DoubleProperty progress = new SimpleDoubleProperty(0);

        progress.addListener((obs, oldValue, newValue) -> {
            double p = newValue.doubleValue();
            fill.setWidth(604 * p);
            percentLabel.setText((int) Math.round(p * 100) + "%");
        });

        Timeline loadingTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress, 0.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(0.55), e -> tipLabel.setText("Routing conveyor belts...")),
                new KeyFrame(Duration.seconds(1.10), e -> tipLabel.setText("Powering Boo's Door...")),
                new KeyFrame(Duration.seconds(1.70), e -> tipLabel.setText("Calibrating monster abilities...")),
                new KeyFrame(Duration.seconds(2.25), e -> tipLabel.setText("Entering the factory floor...")),
                new KeyFrame(Duration.seconds(2.85), new KeyValue(progress, 1.0, Interpolator.EASE_BOTH))
        );

        loadingTimeline.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(ev -> openRealGameScreen(config));
            pause.play();
        });

        loadingTimeline.play();
    }

    static class GameConfig {
        String chosenRole;
        String chosenMonsterName;
        String playerDisplayName;
        String opponentDisplayName;

        GameConfig(String chosenRole, String playerDisplayName, String opponentDisplayName) {
            this(chosenRole, null, playerDisplayName, opponentDisplayName);
        }

        GameConfig(String chosenRole, String chosenMonsterName, String playerDisplayName, String opponentDisplayName) {
            this.chosenRole = chosenRole;
            this.chosenMonsterName = chosenMonsterName;
            this.playerDisplayName = cleanName(playerDisplayName, "Player");
            this.opponentDisplayName = cleanName(opponentDisplayName, "Opponent");
        }

        private static String cleanName(String value, String fallback) {
            if (value == null) return fallback;
            String trimmed = value.trim();
            if (trimmed.length() == 0) return fallback;
            return trimmed;
        }
    }

    static class MonsterProfile {
        String name;
        String role;
        String originalRole;
        String type;
        String personality;
        String passive;
        String powerup;
        int startingEnergy;

        MonsterProfile(String name, String role, String type, String personality, String passive, String powerup, int startingEnergy) {
            this.name = name;
            this.role = role;
            this.originalRole = role;
            this.type = type;
            this.personality = personality;
            this.passive = passive;
            this.powerup = powerup;
            this.startingEnergy = startingEnergy;
        }
    }

    enum NotifySide {
        PLAYER,
        OPPONENT,
        SYSTEM
    }

    static class AudioManager {

        private MediaPlayer menuMusicPlayer;
        private MediaPlayer gameplayMusicPlayer;
        private MediaPlayer victoryMusicPlayer;

        private boolean musicMuted = false;
        private boolean sfxMuted = false;

        private final double musicVolume = 0.26;
        private final double sfxVolume = 0.48;

        public void playMenuMusic() {
            if (musicMuted) return;

            stopMusic();
            menuMusicPlayer = createMusicPlayer("/assets/audio/menu_music.wav");

            if (menuMusicPlayer != null) {
                menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                menuMusicPlayer.setVolume(musicVolume);
                menuMusicPlayer.play();
            }
        }

        public void playGameplayMusic() {
            if (musicMuted) return;

            stopMusic();
            gameplayMusicPlayer = createMusicPlayer("/assets/audio/gameplay_music.wav");

            if (gameplayMusicPlayer != null) {
                gameplayMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                gameplayMusicPlayer.setVolume(musicVolume);
                gameplayMusicPlayer.play();
            }
        }

        public void playVictoryMusic() {
            if (musicMuted) return;

            stopMusic();
            victoryMusicPlayer = createMusicPlayer("/assets/audio/victory_music.wav");

            if (victoryMusicPlayer != null) {
                victoryMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                victoryMusicPlayer.setVolume(musicVolume);
                victoryMusicPlayer.play();
            }
        }

        private MediaPlayer createMusicPlayer(String path) {
            try {
                URL resource = getClass().getResource(path);

                if (resource == null) {
                    System.out.println("Missing music file: " + path);
                    return null;
                }

                return new MediaPlayer(new Media(resource.toExternalForm()));
            } catch (Exception e) {
                System.out.println("Could not load music: " + path);
                return null;
            }
        }

        private void playSfx(String path) {
            if (sfxMuted) return;

            try {
                URL resource = getClass().getResource(path);

                if (resource == null) {
                    System.out.println("Missing sound effect: " + path);
                    return;
                }

                AudioClip clip = new AudioClip(resource.toExternalForm());
                clip.setVolume(sfxVolume);
                clip.play();
            } catch (Exception e) {
                System.out.println("Could not play sound effect: " + path);
            }
        }

        public void playButtonHover() {
            playSfx("/assets/audio/button_hover.wav");
        }

        public void playButtonClick() {
            playSfx("/assets/audio/button_click.wav");
        }

        public void playError() {
            playSfx("/assets/audio/error.wav");
        }

        public void playDiceRoll() {
            playSfx("/assets/audio/dice_roll.wav");
        }

        public void playCardDraw() {
            playSfx("/assets/audio/card_draw.wav");
        }

        public void playEnergyGain() {
            playSfx("/assets/audio/energy_gain.wav");
        }

        public void playEnergyLoss() {
            playSfx("/assets/audio/energy_loss.wav");
        }

        public void playShield() {
            playSfx("/assets/audio/shield.wav");
        }

        public void playConveyor() {
            playSfx("/assets/audio/conveyor.wav");
        }

        public void playCdaAlarm() {
            playSfx("/assets/audio/cda_alarm.wav");
        }

        public void playVictory() {
            playSfx("/assets/audio/victory.wav");
        }
        public void playScarerVictoryRoar() {
            playSfx("/assets/audio/monster_roar_strong.wav");
        }

        public void playLaugherVictoryLaugh() {
            playSfx("/assets/audio/joker_hahaha.wav");
        }
        public void playRoleVictorySound(String role) {
            if ("SCARER".equals(role)) {
                playScarerVictoryRoar();
            } else if ("LAUGHER".equals(role)) {
                playLaugherVictoryLaugh();
            }
        }

        public void toggleMusicMute() {
            musicMuted = !musicMuted;

            if (musicMuted) {
                stopMusic();
            } else {
                playMenuMusic();
            }
        }

        public void toggleSfxMute() {
            sfxMuted = !sfxMuted;
        }

        public boolean isMusicMuted() {
            return musicMuted;
        }

        public boolean isSfxMuted() {
            return sfxMuted;
        }

        public void stopMusic() {
            if (menuMusicPlayer != null) menuMusicPlayer.stop();
            if (gameplayMusicPlayer != null) gameplayMusicPlayer.stop();
            if (victoryMusicPlayer != null) victoryMusicPlayer.stop();
        }

        public void stopAll() {
            stopMusic();
        }
    }

    static class UIEffects {

        public static Button createButton(String text, Color glowColor, AudioManager audioManager, double width, double height) {
            Button button = new Button(text);
            button.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 12));
            button.setTextFill(Color.rgb(245, 248, 255));
            button.setPrefSize(width, height);
            button.setFocusTraversable(false);

            button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgba(24,36,72,0.98), rgba(5,10,26,0.98));" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-color: rgba(235,245,255,0.55);" +
                            "-fx-border-width: 1.4;" +
                            "-fx-border-radius: 16;" +
                            "-fx-cursor: hand;"
            );

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(glowColor, 0.55));
            glow.setRadius(10);
            glow.setSpread(0.10);
            button.setEffect(glow);

            button.setOnMouseEntered(e -> {
                if (audioManager != null) audioManager.playButtonHover();

                ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
                scale.setToX(1.035);
                scale.setToY(1.035);
                scale.play();

                glow.setRadius(20);
                glow.setSpread(0.25);
            });

            button.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
                scale.setToX(1);
                scale.setToY(1);
                scale.play();

                glow.setRadius(10);
                glow.setSpread(0.10);
            });

            button.setOnMousePressed(e -> {
                if (audioManager != null) audioManager.playButtonClick();

                ScaleTransition scale = new ScaleTransition(Duration.millis(60), button);
                scale.setToX(0.96);
                scale.setToY(0.96);
                scale.play();
            });

            button.setOnMouseReleased(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(80), button);
                scale.setToX(1.025);
                scale.setToY(1.025);
                scale.play();
            });

            return button;
        }

        public static Label sectionTitle(String text, Color color) {
            Label label = new Label(text);
            label.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 21));
            label.setTextFill(Color.rgb(245, 250, 255));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.52));
            glow.setRadius(12);
            label.setEffect(glow);

            return label;
        }

        public static void floating(Node node, double distance, double seconds) {
            TranslateTransition transition = new TranslateTransition(Duration.seconds(seconds), node);
            transition.setFromY(-distance);
            transition.setToY(distance);
            transition.setAutoReverse(true);
            transition.setCycleCount(Animation.INDEFINITE);
            transition.setInterpolator(Interpolator.EASE_BOTH);
            transition.play();
        }

        public static void pulse(Node node, Color color) {
            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.62));
            glow.setRadius(13);
            glow.setSpread(0.14);
            node.setEffect(glow);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(glow.radiusProperty(), 11),
                            new KeyValue(glow.spreadProperty(), 0.10)
                    ),
                    new KeyFrame(Duration.seconds(1.7),
                            new KeyValue(glow.radiusProperty(), 24),
                            new KeyValue(glow.spreadProperty(), 0.28)
                    ),
                    new KeyFrame(Duration.seconds(3.4),
                            new KeyValue(glow.radiusProperty(), 11),
                            new KeyValue(glow.spreadProperty(), 0.10)
                    )
            );

            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    private static Color withAlpha(Color color, double alpha) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    private static Rectangle createUniversalVignette() {
        Rectangle vignette = new Rectangle(APP_WIDTH, APP_HEIGHT);

        vignette.setFill(new RadialGradient(
                0, 0,
                0.5, 0.48,
                0.85,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.rgb(0, 0, 0, 0.00)),
                new Stop(0.55, Color.rgb(0, 0, 0, 0.10)),
                new Stop(1.00, Color.rgb(0, 0, 0, 0.42))
        ));

        vignette.setMouseTransparent(true);
        return vignette;
    }
    private static final Map<String, Image> ROLE_IMAGE_CACHE = new HashMap<String, Image>();
    private static Image loadRoleImage(String role) {
        String imagePath = "SCARER".equals(role)
                ? "/assets/images/scarer_character.png"
                : "/assets/images/laugher_character.png";

        if (ROLE_IMAGE_CACHE.containsKey(imagePath)) {
            return ROLE_IMAGE_CACHE.get(imagePath);
        }

        try {
            URL url = MainApp.class.getResource(imagePath);

            if (url == null) {
                System.out.println("Missing image: " + imagePath);
                return null;
            }

            Image raw = new Image(url.toExternalForm());
            Image cleaned = removeNearWhiteBackground(raw);

            ROLE_IMAGE_CACHE.put(imagePath, cleaned);
            return cleaned;

        } catch (Exception e) {
            System.out.println("Could not load character image: " + imagePath);
            return null;
        }
    }

    private static Image removeNearWhiteBackground(Image source) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();

        WritableImage output = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = reader.getColor(x, y);

                double r = c.getRed();
                double g = c.getGreen();
                double b = c.getBlue();
                double a = c.getOpacity();

                double brightness = (r + g + b) / 3.0;
                double spread = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));

                if (a < 0.05) {
                    writer.setColor(x, y, Color.TRANSPARENT);
                } else if (brightness > 0.95 && spread < 0.18) {
                    writer.setColor(x, y, Color.color(r, g, b, 0.0));
                } else if (brightness > 0.88 && spread < 0.20) {
                    writer.setColor(x, y, Color.color(r, g, b, 0.10));
                } else {
                    writer.setColor(x, y, c);
                }
            }
        }

        return output;
    }

    private static StackPane createCharacterCutout(MonsterProfile monster, Color glowColor,
                                                   double width, double height, double fitHeight) {
        StackPane holder = new StackPane();
        holder.setPrefSize(width, height);
        holder.setMinSize(width, height);
        holder.setMaxSize(width, height);
        holder.setPickOnBounds(false);

        Ellipse aura = new Ellipse(width * 0.26, height * 0.30);
        aura.setFill(Color.rgb(8, 14, 30, 0.10));
        aura.setStroke(withAlpha(glowColor, 0.28));
        aura.setStrokeWidth(1.2);

        DropShadow auraGlow = new DropShadow();
        auraGlow.setColor(withAlpha(glowColor, 0.30));
        auraGlow.setRadius(18);
        auraGlow.setSpread(0.12);
        aura.setEffect(auraGlow);

        Image image = loadRoleImage(monster.role);

        if (image != null) {
            ImageView art = new ImageView(image);
            art.setPreserveRatio(true);
            art.setFitHeight(fitHeight);
            art.setSmooth(true);

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(glowColor, 0.68));
            glow.setRadius(24);
            glow.setSpread(0.20);
            art.setEffect(glow);

            TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(2.8), art);
            floatAnim.setFromY(-4);
            floatAnim.setToY(4);
            floatAnim.setAutoReverse(true);
            floatAnim.setCycleCount(Animation.INDEFINITE);
            floatAnim.setInterpolator(Interpolator.EASE_BOTH);
            floatAnim.play();

            ScaleTransition breathe = new ScaleTransition(Duration.seconds(3.2), art);
            breathe.setFromX(1.0);
            breathe.setFromY(1.0);
            breathe.setToX(1.03);
            breathe.setToY(1.03);
            breathe.setAutoReverse(true);
            breathe.setCycleCount(Animation.INDEFINITE);
            breathe.setInterpolator(Interpolator.EASE_BOTH);
            breathe.play();

            holder.getChildren().addAll(aura, art);
        } else {
            StackPane fallback = MonsterArt.createMonster(monster.role, monster.type, true, fitHeight);
            holder.getChildren().addAll(aura, fallback);
        }

        return holder;
    }

    static class MonsterArt {

        public static StackPane createMonster(String role, String type, boolean player, double size) {
            StackPane root = new StackPane();
            root.setPrefSize(size, size);
            root.setMaxSize(size, size);

            Color main;
            Color secondary;
            Color glowColor;

            if ("SCARER".equals(role)) {
                main = Color.rgb(83, 112, 220);
                secondary = Color.rgb(100, 60, 175);
                glowColor = Color.rgb(95, 145, 245);
            } else {
                main = Color.rgb(72, 185, 125);
                secondary = Color.rgb(230, 205, 70);
                glowColor = Color.rgb(80, 225, 160);
            }

            Circle floorShadow = new Circle(size * 0.32);
            floorShadow.setFill(Color.rgb(0, 0, 0, 0.35));
            floorShadow.setScaleY(0.23);
            floorShadow.setTranslateY(size * 0.38);

            Circle aura = new Circle(size * 0.43);
            aura.setFill(Color.rgb(5, 10, 24, 0.92));
            aura.setStroke(withAlpha(glowColor, 0.80));
            aura.setStrokeWidth(size * 0.020);

            DropShadow auraGlow = new DropShadow();
            auraGlow.setColor(withAlpha(glowColor, 0.52));
            auraGlow.setRadius(size * 0.14);
            auraGlow.setSpread(0.18);
            aura.setEffect(auraGlow);

            Ellipse backBody = new Ellipse(size * 0.27, size * 0.32);
            backBody.setFill(secondary.darker());
            backBody.setTranslateX(size * 0.035);
            backBody.setTranslateY(size * 0.025);

            Ellipse body = new Ellipse(size * 0.28, size * 0.33);
            body.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, main.brighter()),
                    new Stop(0.42, main),
                    new Stop(1, secondary.darker())
            ));
            body.setStroke(Color.rgb(235, 245, 255, 0.82));
            body.setStrokeWidth(size * 0.011);

            Polygon leftHorn = new Polygon(
                    -size * 0.24, -size * 0.28,
                    -size * 0.12, -size * 0.46,
                    -size * 0.03, -size * 0.27
            );
            leftHorn.setFill(Color.rgb(230, 220, 185));
            leftHorn.setStroke(Color.rgb(255, 245, 210, 0.8));
            leftHorn.setStrokeWidth(size * 0.005);

            Polygon rightHorn = new Polygon(
                    size * 0.24, -size * 0.28,
                    size * 0.12, -size * 0.46,
                    size * 0.03, -size * 0.27
            );
            rightHorn.setFill(Color.rgb(230, 220, 185));
            rightHorn.setStroke(Color.rgb(255, 245, 210, 0.8));
            rightHorn.setStrokeWidth(size * 0.005);

            Circle eyeWhite = new Circle(size * 0.10);
            eyeWhite.setFill(Color.rgb(245, 250, 255));
            eyeWhite.setTranslateY(-size * 0.075);

            Circle pupil = new Circle(size * 0.040);
            pupil.setFill(Color.rgb(20, 30, 45));
            pupil.setTranslateY(-size * 0.075);

            Circle shine = new Circle(size * 0.012);
            shine.setFill(Color.WHITE);
            shine.setTranslateX(size * 0.020);
            shine.setTranslateY(-size * 0.098);

            Circle cheek1 = new Circle(size * 0.034);
            cheek1.setFill(withAlpha(secondary, 0.50));
            cheek1.setTranslateX(-size * 0.12);
            cheek1.setTranslateY(size * 0.062);

            Circle cheek2 = new Circle(size * 0.034);
            cheek2.setFill(withAlpha(secondary, 0.50));
            cheek2.setTranslateX(size * 0.12);
            cheek2.setTranslateY(size * 0.062);

            Rectangle mouth = new Rectangle(size * 0.16, size * 0.025);
            mouth.setArcWidth(size * 0.025);
            mouth.setArcHeight(size * 0.025);
            mouth.setFill(Color.rgb(25, 20, 32, 0.78));
            mouth.setTranslateY(size * 0.145);

            Label typeBadge = new Label(shortType(type));
            typeBadge.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, size * 0.07));
            typeBadge.setTextFill(Color.WHITE);
            typeBadge.setPadding(new Insets(3, 7, 3, 7));
            typeBadge.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 12, 28, 0.82),
                    new CornerRadii(10),
                    Insets.EMPTY
            )));
            typeBadge.setBorder(new Border(new BorderStroke(
                    withAlpha(glowColor, 0.7),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1)
            )));
            typeBadge.setTranslateY(size * 0.34);

            root.getChildren().addAll(
                    floorShadow,
                    aura,
                    leftHorn,
                    rightHorn,
                    backBody,
                    body,
                    cheek1,
                    cheek2,
                    eyeWhite,
                    pupil,
                    shine,
                    mouth,
                    typeBadge
            );

            UIEffects.floating(root, size * 0.014, 3.0);
            addBlinkAnimation(eyeWhite, pupil, shine);

            return root;
        }

        private static void addBlinkAnimation(Node eye, Node pupil, Node shine) {
            Timeline blink = new Timeline(
                    new KeyFrame(Duration.seconds(2.2), e -> {
                        ScaleTransition closeEye = new ScaleTransition(Duration.millis(80), eye);
                        closeEye.setToY(0.12);

                        ScaleTransition closePupil = new ScaleTransition(Duration.millis(80), pupil);
                        closePupil.setToY(0.12);

                        ScaleTransition closeShine = new ScaleTransition(Duration.millis(80), shine);
                        closeShine.setToY(0.12);

                        ParallelTransition close = new ParallelTransition(closeEye, closePupil, closeShine);

                        ScaleTransition openEye = new ScaleTransition(Duration.millis(90), eye);
                        openEye.setToY(1);

                        ScaleTransition openPupil = new ScaleTransition(Duration.millis(90), pupil);
                        openPupil.setToY(1);

                        ScaleTransition openShine = new ScaleTransition(Duration.millis(90), shine);
                        openShine.setToY(1);

                        new SequentialTransition(close, new ParallelTransition(openEye, openPupil, openShine)).play();
                    })
            );

            blink.setCycleCount(Animation.INDEFINITE);
            blink.play();
        }

        public static StackPane createSmallMonsterToken(String role, String type, boolean player) {
            StackPane root = new StackPane();
            root.setPrefSize(30, 30);
            root.setMaxSize(30, 30);
            root.setPickOnBounds(false);

            Color glowColor = player ? Color.rgb(80, 225, 160) : Color.rgb(235, 95, 95);

            Circle floorGlow = new Circle(12);
            floorGlow.setFill(withAlpha(glowColor, 0.22));
            floorGlow.setScaleY(0.35);
            floorGlow.setTranslateY(12);

            Circle aura = new Circle(13);
            aura.setFill(Color.rgb(5, 10, 24, 0.72));
            aura.setStroke(withAlpha(glowColor, 0.95));
            aura.setStrokeWidth(1.7);

            DropShadow auraGlow = new DropShadow();
            auraGlow.setColor(withAlpha(glowColor, 0.75));
            auraGlow.setRadius(10);
            auraGlow.setSpread(0.26);
            aura.setEffect(auraGlow);

            Image image = loadRoleImage(role);

            if (image != null) {
                ImageView art = new ImageView(image);
                art.setPreserveRatio(true);
                art.setFitHeight(27);
                art.setSmooth(true);
                art.setTranslateY(-2);

                DropShadow characterGlow = new DropShadow();
                characterGlow.setColor(withAlpha(glowColor, 0.88));
                characterGlow.setRadius(8);
                characterGlow.setSpread(0.22);
                art.setEffect(characterGlow);

                Label letter = new Label(player ? "P" : "O");
                letter.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 7));
                letter.setTextFill(Color.WHITE);
                letter.setPadding(new Insets(1, 4, 1, 4));
                letter.setBackground(new Background(new BackgroundFill(
                        Color.rgb(5, 8, 20, 0.88),
                        new CornerRadii(8),
                        Insets.EMPTY
                )));
                letter.setTranslateY(12);

                root.getChildren().addAll(floorGlow, aura, art, letter);

            } else {
                Color secondary = "SCARER".equals(role) ? Color.rgb(100, 60, 175) : Color.rgb(230, 205, 70);

                Circle faceBack = new Circle(10);
                faceBack.setFill(secondary.darker());
                faceBack.setTranslateX(1.5);
                faceBack.setTranslateY(1.5);

                Circle face = new Circle(10);
                face.setFill(glowColor);
                face.setStroke(Color.WHITE);
                face.setStrokeWidth(1.2);

                Circle eye = new Circle(2.8);
                eye.setFill(Color.WHITE);
                eye.setTranslateY(-2);

                Circle pupil = new Circle(1.1);
                pupil.setFill(Color.rgb(20, 25, 35));
                pupil.setTranslateY(-2);

                Label letter = new Label(player ? "P" : "O");
                letter.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 7));
                letter.setTextFill(Color.WHITE);
                letter.setTranslateY(7);

                root.getChildren().addAll(floorGlow, aura, faceBack, face, eye, pupil, letter);
            }

            return root;
        }

        private static String shortType(String type) {
            if (type == null) return "?";
            if (type.equalsIgnoreCase("Dynamo")) return "DYN";
            if (type.equalsIgnoreCase("Dasher")) return "DASH";
            if (type.equalsIgnoreCase("Multitasker")) return "MULTI";
            if (type.equalsIgnoreCase("Schemer")) return "SCH";
            return type.toUpperCase();
        }
    }

    static class CinematicBackground {

        public static StackPane createRoot() {
            StackPane root = new StackPane();
            root.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Pane background = new Pane();
            background.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Rectangle base = new Rectangle(APP_WIDTH, APP_HEIGHT);
            base.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(6, 7, 18)),
                    new Stop(0.35, Color.rgb(15, 13, 36)),
                    new Stop(0.72, Color.rgb(4, 26, 32)),
                    new Stop(1, Color.rgb(2, 4, 10))
            ));

            background.getChildren().add(base);

            createFactoryDoors(background);

            Rectangle vignette = new Rectangle(APP_WIDTH, APP_HEIGHT);
            vignette.setFill(new RadialGradient(
                    0, 0,
                    0.5, 0.47,
                    0.85,
                    true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(0, 0, 0, 0.04)),
                    new Stop(0.58, Color.rgb(0, 0, 0, 0.28)),
                    new Stop(1, Color.rgb(0, 0, 0, 0.72))
            ));

            Rectangle overlay = new Rectangle(APP_WIDTH, APP_HEIGHT);
            overlay.setFill(Color.rgb(0, 0, 0, 0.14));

            root.getChildren().addAll(background, vignette, overlay, createUniversalVignette());
            return root;
        }

        private static void createFactoryDoors(Pane background) {
            Random random = new Random();

            for (int i = 0; i < 12; i++) {
                double x = 55 + random.nextDouble() * (APP_WIDTH - 110);
                double y = 70 + random.nextDouble() * (APP_HEIGHT - 170);

                Rectangle door = new Rectangle(42, 72);
                door.setArcWidth(9);
                door.setArcHeight(9);

                Color fill;

                if (i % 3 == 0) fill = Color.rgb(40, 85, 135, 0.20);
                else if (i % 3 == 1) fill = Color.rgb(35, 125, 90, 0.18);
                else fill = Color.rgb(95, 55, 130, 0.16);

                door.setFill(fill);
                door.setStroke(Color.rgb(220, 240, 255, 0.08));
                door.setStrokeWidth(1);
                door.setLayoutX(x);
                door.setLayoutY(y);
                door.setOpacity(0.34);

                background.getChildren().add(door);
              
            }
        }

        private static void createSoftParticles(Pane background) {
            Random random = new Random();

            for (int i = 0; i < 42; i++) {
                Circle particle = new Circle(1.1 + random.nextDouble() * 1.5);

                if (i % 2 == 0) particle.setFill(Color.rgb(80, 210, 255, 0.20));
                else particle.setFill(Color.rgb(255, 225, 110, 0.16));

                particle.setLayoutX(random.nextDouble() * APP_WIDTH);
                particle.setLayoutY(random.nextDouble() * APP_HEIGHT);

                background.getChildren().add(particle);

                TranslateTransition move = new TranslateTransition(Duration.seconds(8 + random.nextDouble() * 8), particle);
                move.setFromY(0);
                move.setToY(-40 - random.nextDouble() * 80);
                move.setCycleCount(Animation.INDEFINITE);

                FadeTransition fade = new FadeTransition(Duration.seconds(6 + random.nextDouble() * 6), particle);
                fade.setFromValue(0.05);
                fade.setToValue(0.35);
                fade.setCycleCount(Animation.INDEFINITE);
                fade.setAutoReverse(true);

                new ParallelTransition(move, fade).play();
            }
        }

        private static void createFog(Pane background) {
            Rectangle fog1 = new Rectangle(APP_WIDTH + 200, 210);
            fog1.setFill(Color.rgb(160, 200, 255, 0.035));
            fog1.setEffect(new BoxBlur(70, 70, 3));
            fog1.setLayoutX(-100);
            fog1.setLayoutY(135);

            Rectangle fog2 = new Rectangle(APP_WIDTH + 240, 230);
            fog2.setFill(Color.rgb(145, 255, 210, 0.026));
            fog2.setEffect(new BoxBlur(80, 80, 3));
            fog2.setLayoutX(-140);
            fog2.setLayoutY(455);

            background.getChildren().addAll(fog1, fog2);

            TranslateTransition drift1 = new TranslateTransition(Duration.seconds(15), fog1);
            drift1.setFromX(-70);
            drift1.setToX(70);
            drift1.setAutoReverse(true);
            drift1.setCycleCount(Animation.INDEFINITE);
            drift1.play();

            TranslateTransition drift2 = new TranslateTransition(Duration.seconds(18), fog2);
            drift2.setFromX(90);
            drift2.setToX(-90);
            drift2.setAutoReverse(true);
            drift2.setCycleCount(Animation.INDEFINITE);
            drift2.play();
        }
    }

    static class StartScreenView {

        private final StackPane root;
        private final AudioManager audioManager;
        private final java.util.function.Consumer<GameConfig> onStartGame;
        private final Runnable onInstructions;
        private final Runnable onToggleFullScreen;
        private final Runnable onExit;

        private String selectedRole = null;

        private VBox scarerPanel;
        private VBox laugherPanel;
        private Label selectedLabel;
        private Label versusLabel;
        private TextField playerNameField;
        private TextField opponentNameField;

        private final MonsterProfile scarerPreview = new MonsterProfile(
                "James P. Sullivan",
                "SCARER",
                "Dynamo",
                "A powerhouse monster built for huge energy swings.",
                "Energy gains and losses are doubled.",
                "Energy Freeze: freezes the opponent for one turn.",
                300
        );

        private final MonsterProfile laugherPreview = new MonsterProfile(
                "Mike Wazowski",
                "LAUGHER",
                "Dasher",
                "A fast comedy monster who wins through pressure and speed.",
                "Base dice movement is doubled.",
                "Momentum Rush: movement becomes 3x for 3 turns.",
                100
        );
        StartScreenView(AudioManager audioManager, java.util.function.Consumer<GameConfig> onStartGame, Runnable onInstructions, Runnable onToggleFullScreen, Runnable onExit) {
            this.audioManager = audioManager;
            this.onStartGame = onStartGame;
            this.onInstructions = onInstructions;
            this.onToggleFullScreen = onToggleFullScreen;
            this.onExit = onExit;
            root = createAnimatedStartBackground();

            build();
            playIntroAnimation();
        }

        public Parent getRoot() {
            return root;
        }
        private StackPane createAnimatedStartBackground() {
            StackPane sceneRoot = new StackPane();
            sceneRoot.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Image image = null;

            try {
                URL imageUrl = getClass().getResource("/assets/images/start_background.png");

                if (imageUrl != null) {
                    image = new Image(imageUrl.toExternalForm());
                } else {
                    System.out.println("Missing image: /assets/images/start_background.png");
                }
            } catch (Exception e) {
                System.out.println("Could not load start background image.");
            }

            if (image == null) {
                return CinematicBackground.createRoot();
            }

            ImageView background = new ImageView(image);
            background.setFitWidth(APP_WIDTH);
            background.setFitHeight(APP_HEIGHT);
            background.setPreserveRatio(false);
            background.setSmooth(true);
            background.setCache(true);

            background.setScaleX(1.0);
            background.setScaleY(1.0);
            background.setTranslateX(0);
            background.setTranslateY(0);

            background.setScaleX(1.04);
            background.setScaleY(1.04);
            background.setTranslateX(0);
            background.setTranslateY(0);

            Rectangle readabilityOverlay = new Rectangle(APP_WIDTH, APP_HEIGHT);
            readabilityOverlay.setFill(new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.rgb(0, 0, 0, 0.30)),
                    new Stop(0.28, Color.rgb(0, 0, 0, 0.12)),
                    new Stop(0.58, Color.rgb(0, 0, 0, 0.20)),
                    new Stop(1.00, Color.rgb(0, 0, 0, 0.62))
            ));

            Rectangle centerDarkGlass = new Rectangle(APP_WIDTH, APP_HEIGHT);
            centerDarkGlass.setFill(new RadialGradient(
                    0, 0,
                    0.5, 0.54,
                    0.74,
                    true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.rgb(0, 0, 0, 0.05)),
                    new Stop(0.45, Color.rgb(0, 0, 0, 0.18)),
                    new Stop(1.00, Color.rgb(0, 0, 0, 0.58))
            ));

            Pane particles = new Pane();
            particles.setMouseTransparent(true);

            Pane fog = new Pane();
            fog.setMouseTransparent(true);

            Pane lightSweep = new Pane();
            lightSweep.setMouseTransparent(true);
            sceneRoot.getChildren().addAll(
                    background,
                    readabilityOverlay,
                    centerDarkGlass,
                    createUniversalVignette()
            );

            return sceneRoot;
        }
        private Pane createStartScreenParticles() {
            Pane particles = new Pane();
            particles.setPrefSize(APP_WIDTH, APP_HEIGHT);
            particles.setMouseTransparent(true);

            Random random = new Random();

            for (int i = 0; i < 75; i++) {
                Circle p = new Circle(1.0 + random.nextDouble() * 2.2);

                if (i % 3 == 0) {
                    p.setFill(Color.rgb(90, 180, 255, 0.38));
                } else if (i % 3 == 1) {
                    p.setFill(Color.rgb(110, 255, 165, 0.32));
                } else {
                    p.setFill(Color.rgb(230, 170, 255, 0.28));
                }

                p.setLayoutX(random.nextDouble() * APP_WIDTH);
                p.setLayoutY(random.nextDouble() * APP_HEIGHT);

                particles.getChildren().add(p);

                TranslateTransition drift = new TranslateTransition(Duration.seconds(5.5 + random.nextDouble() * 7), p);
                drift.setFromY(20);
                drift.setToY(-70 - random.nextDouble() * 80);
                drift.setFromX(-12 + random.nextDouble() * 24);
                drift.setToX(-30 + random.nextDouble() * 60);
                drift.setCycleCount(Animation.INDEFINITE);
                drift.setInterpolator(Interpolator.LINEAR);

                FadeTransition blink = new FadeTransition(Duration.seconds(2.5 + random.nextDouble() * 3), p);
                blink.setFromValue(0.12);
                blink.setToValue(0.85);
                blink.setAutoReverse(true);
                blink.setCycleCount(Animation.INDEFINITE);

                new ParallelTransition(drift, blink).play();
            }

            return particles;
        }

        private Pane createStartScreenFog() {
            Pane fogPane = new Pane();
            fogPane.setPrefSize(APP_WIDTH, APP_HEIGHT);
            fogPane.setMouseTransparent(true);

            Rectangle blueFog = new Rectangle(APP_WIDTH + 260, 240);
            blueFog.setFill(Color.rgb(80, 120, 255, 0.075));
            blueFog.setEffect(new BoxBlur(85, 85, 3));
            blueFog.setLayoutX(-150);
            blueFog.setLayoutY(430);

            Rectangle greenFog = new Rectangle(APP_WIDTH + 260, 230);
            greenFog.setFill(Color.rgb(80, 255, 170, 0.060));
            greenFog.setEffect(new BoxBlur(90, 90, 3));
            greenFog.setLayoutX(-120);
            greenFog.setLayoutY(485);

            Rectangle purpleFog = new Rectangle(APP_WIDTH + 220, 180);
            purpleFog.setFill(Color.rgb(200, 90, 255, 0.045));
            purpleFog.setEffect(new BoxBlur(85, 85, 3));
            purpleFog.setLayoutX(-120);
            purpleFog.setLayoutY(250);

            fogPane.getChildren().addAll(blueFog, greenFog, purpleFog);

            TranslateTransition blueMove = new TranslateTransition(Duration.seconds(14), blueFog);
            blueMove.setFromX(-70);
            blueMove.setToX(80);
            blueMove.setAutoReverse(true);
            blueMove.setCycleCount(Animation.INDEFINITE);
            blueMove.setInterpolator(Interpolator.EASE_BOTH);
            blueMove.play();

            TranslateTransition greenMove = new TranslateTransition(Duration.seconds(17), greenFog);
            greenMove.setFromX(85);
            greenMove.setToX(-85);
            greenMove.setAutoReverse(true);
            greenMove.setCycleCount(Animation.INDEFINITE);
            greenMove.setInterpolator(Interpolator.EASE_BOTH);
            greenMove.play();

            TranslateTransition purpleMove = new TranslateTransition(Duration.seconds(19), purpleFog);
            purpleMove.setFromX(-60);
            purpleMove.setToX(60);
            purpleMove.setAutoReverse(true);
            purpleMove.setCycleCount(Animation.INDEFINITE);
            purpleMove.setInterpolator(Interpolator.EASE_BOTH);
            purpleMove.play();

            return fogPane;
        }

        private Pane createStartScreenLightSweep() {
            Pane pane = new Pane();
            pane.setPrefSize(APP_WIDTH, APP_HEIGHT);
            pane.setMouseTransparent(true);

            Rectangle sweep = new Rectangle(160, APP_HEIGHT + 180);
            sweep.setFill(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 255, 255, 0.00)),
                    new Stop(0.5, Color.rgb(150, 230, 255, 0.13)),
                    new Stop(1, Color.rgb(255, 255, 255, 0.00))
            ));

            sweep.setRotate(18);
            sweep.setLayoutX(-260);
            sweep.setLayoutY(-90);
            sweep.setEffect(new BoxBlur(28, 28, 2));

            pane.getChildren().add(sweep);

            TranslateTransition move = new TranslateTransition(Duration.seconds(7.5), sweep);
            move.setFromX(-220);
            move.setToX(APP_WIDTH + 360);
            move.setCycleCount(Animation.INDEFINITE);
            move.setInterpolator(Interpolator.EASE_BOTH);
            move.play();

            FadeTransition pulse = new FadeTransition(Duration.seconds(3.8), sweep);
            pulse.setFromValue(0.15);
            pulse.setToValue(0.52);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            return pane;
        }

        private void buildBattleBackground() {
            Pane arena = new Pane();
            arena.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Rectangle leftGlow = new Rectangle(APP_WIDTH / 2.0, APP_HEIGHT);
            leftGlow.setFill(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(60, 95, 190, 0.25)),
                    new Stop(1, Color.rgb(60, 95, 190, 0.00))
            ));

            Rectangle rightGlow = new Rectangle(APP_WIDTH / 2.0, APP_HEIGHT);
            rightGlow.setLayoutX(APP_WIDTH / 2.0);
            rightGlow.setFill(new LinearGradient(
                    1, 0, 0, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(65, 210, 145, 0.25)),
                    new Stop(1, Color.rgb(65, 210, 145, 0.00))
            ));

            arena.getChildren().addAll(leftGlow, rightGlow);

            createPixelStorm(arena, Color.rgb(95, 145, 245), true);
            createPixelStorm(arena, Color.rgb(75, 220, 155), false);
            createEnergyBeams(arena);

            root.getChildren().add(arena);
        }

        private void createPixelStorm(Pane arena, Color color, boolean left) {
            Random random = new Random();

            for (int i = 0; i < 36; i++) {
                Rectangle pixel = new Rectangle(5 + random.nextInt(7), 5 + random.nextInt(7));
                pixel.setFill(withAlpha(color, 0.18 + random.nextDouble() * 0.25));
                pixel.setArcWidth(2);
                pixel.setArcHeight(2);

                double x = left ? random.nextDouble() * 500 : 860 + random.nextDouble() * 460;
                double y = 90 + random.nextDouble() * 560;

                pixel.setLayoutX(x);
                pixel.setLayoutY(y);

                arena.getChildren().add(pixel);

                TranslateTransition drift = new TranslateTransition(Duration.seconds(3.5 + random.nextDouble() * 3.5), pixel);
                drift.setFromY(-10);
                drift.setToY(18);
                drift.setAutoReverse(true);
                drift.setCycleCount(Animation.INDEFINITE);
                drift.play();

                FadeTransition fade = new FadeTransition(Duration.seconds(2.5 + random.nextDouble() * 3), pixel);
                fade.setFromValue(0.15);
                fade.setToValue(0.65);
                fade.setAutoReverse(true);
                fade.setCycleCount(Animation.INDEFINITE);
                fade.play();
            }
        }

        private void createEnergyBeams(Pane arena) {
            for (int i = 0; i < 8; i++) {
                Rectangle beam = new Rectangle(180, 2);
                beam.setArcWidth(2);
                beam.setArcHeight(2);
                beam.setFill(i % 2 == 0 ? Color.rgb(95, 145, 245, 0.25) : Color.rgb(75, 220, 155, 0.25));
                beam.setLayoutY(170 + i * 55);
                beam.setLayoutX(i % 2 == 0 ? 220 : 960);
                arena.getChildren().add(beam);

                TranslateTransition move = new TranslateTransition(Duration.seconds(2.8 + i * 0.2), beam);
                move.setFromX(i % 2 == 0 ? -30 : 30);
                move.setToX(i % 2 == 0 ? 60 : -60);
                move.setAutoReverse(true);
                move.setCycleCount(Animation.INDEFINITE);
                move.play();

                FadeTransition fade = new FadeTransition(Duration.seconds(2.2), beam);
                fade.setFromValue(0.15);
                fade.setToValue(0.60);
                fade.setAutoReverse(true);
                fade.setCycleCount(Animation.INDEFINITE);
                fade.play();
            }
        }

        private void build() {
            BorderPane main = new BorderPane();
            main.setPadding(new Insets(24, 56, 30, 56));

            main.setTop(createTitleArea());
            main.setCenter(createVersusArea());
            main.setBottom(createBottomArea());

            root.getChildren().add(main);
        }

        private VBox createTitleArea() {
            VBox box = new VBox(3);
            box.setAlignment(Pos.CENTER);

            Label title = new Label("DooR DasH");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 58));
            title.setTextFill(Color.rgb(248, 252, 255));

            Label subtitle = new Label("Scare vs Laugh Touchdown");
            subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
            subtitle.setTextFill(Color.rgb(185, 225, 245));

            Label tagline = new Label("Choose your side. Race the factory. Open Boo's Door.");
            tagline.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            tagline.setTextFill(Color.rgb(210, 220, 230, 0.82));

            UIEffects.pulse(title, Color.rgb(105, 220, 255));

            box.getChildren().addAll(title, subtitle, tagline);
            return box;
        }

        private HBox createVersusArea() {
            HBox arena = new HBox(38);
            arena.setAlignment(Pos.CENTER);
            arena.setPadding(new Insets(10, 0, 6, 0));

            scarerPanel = createCharacterSelectionPanel(scarerPreview, Color.rgb(95, 145, 245));
            laugherPanel = createCharacterSelectionPanel(laugherPreview, Color.rgb(75, 220, 155));

            StackPane middle = createVersusCore();

            scarerPanel.setOnMouseClicked(e -> selectRole("SCARER"));
            laugherPanel.setOnMouseClicked(e -> selectRole("LAUGHER"));

            arena.getChildren().addAll(scarerPanel, middle, laugherPanel);
            return arena;
        }
        private StackPane createStartScreenCharacterArt(MonsterProfile monster, Color glowColor) {
            return createCharacterCutout(monster, glowColor, 165, 125, 110);
        }
        private VBox createCharacterSelectionPanel(MonsterProfile monster, Color color) {
            VBox panel = new VBox(8);
            panel.setAlignment(Pos.CENTER);
            panel.setPadding(new Insets(14));
            panel.setPrefSize(360, 350);
            panel.setMaxSize(360, 350);

            panel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(7, 12, 28, 0.42),
                    new CornerRadii(28),
                    Insets.EMPTY
            )));

            panel.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.72),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(1.8)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.36));
            glow.setRadius(18);
            glow.setSpread(0.10);
            panel.setEffect(glow);

            StackPane characterArt = createStartScreenCharacterArt(monster, color);

            Label role = new Label(monster.role);
            role.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 25));
            role.setTextFill(Color.rgb(248, 252, 255));

            Label name = new Label(monster.name + "  •  " + monster.type);
            name.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 11));
            name.setTextFill(color);

            Label passive = new Label(monster.passive);
            passive.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
            passive.setTextFill(Color.rgb(218, 228, 238));
            passive.setWrapText(true);
            passive.setMaxWidth(265);
            passive.setAlignment(Pos.CENTER);

            Label choose = new Label("CLICK TO LOCK THIS SIDE");
            choose.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 9));
            choose.setTextFill(Color.rgb(210, 220, 230, 0.84));

            panel.getChildren().addAll(characterArt, role, name, passive, choose);

            panel.setOnMouseEntered(e -> {
                audioManager.playButtonHover();

                ScaleTransition scale = new ScaleTransition(Duration.millis(140), panel);
                scale.setToX(1.025);
                scale.setToY(1.025);
                scale.play();

                glow.setRadius(28);
                glow.setSpread(0.21);
            });

            panel.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(140), panel);
                scale.setToX(1);
                scale.setToY(1);
                scale.play();

                if (!monster.role.equals(selectedRole)) {
                    glow.setRadius(18);
                    glow.setSpread(0.10);
                }
            });

            return panel;
        }

        private StackPane createVersusCore() {
            StackPane core = new StackPane();
            core.setPrefSize(230, 410);

            VBox box = new VBox(16);
            box.setAlignment(Pos.CENTER);

            versusLabel = new Label("VS");
            versusLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 66));
            versusLabel.setTextFill(Color.rgb(245, 250, 255));
            UIEffects.pulse(versusLabel, Color.rgb(220, 145, 230));

            Rectangle lineTop = new Rectangle(150, 2);
            lineTop.setFill(Color.rgb(125, 205, 235, 0.52));

            Rectangle lineBottom = new Rectangle(150, 2);
            lineBottom.setFill(Color.rgb(125, 205, 235, 0.52));

            box.getChildren().addAll(lineTop, versusLabel, lineBottom);
            core.getChildren().add(box);

            return core;
        }

        private VBox createBottomArea() {
            VBox bottom = new VBox(12);
            bottom.setAlignment(Pos.CENTER);

            selectedLabel = new Label("Select your monster side and enter the factory floor.");
            selectedLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
            selectedLabel.setTextFill(Color.rgb(230, 238, 245));

            HBox names = new HBox(16);
            names.setAlignment(Pos.CENTER);

            playerNameField = createNameField("Daniel");
            opponentNameField = createNameField("Rival");

            names.getChildren().addAll(
                    nameBox("PLAYER NAME", playerNameField, Color.rgb(95, 145, 245)),
                    nameBox("OPPONENT NAME", opponentNameField, Color.rgb(75, 220, 155))
            );

            HBox buttons = new HBox(18);
            buttons.setAlignment(Pos.CENTER);
            Button start = UIEffects.createButton("START MATCH", Color.rgb(80, 230, 165), audioManager, 175, 42);
            Button instructions = UIEffects.createButton("INSTRUCTIONS", Color.rgb(105, 190, 245), audioManager, 165, 42);
            Button fullScreen = UIEffects.createButton("FULL SCREEN", Color.rgb(215, 130, 235), audioManager, 140, 42);
            Button exit = UIEffects.createButton("EXIT", Color.rgb(235, 105, 105), audioManager, 120, 42);
            Button music = UIEffects.createButton("MUSIC ON", Color.rgb(170, 210, 235), audioManager, 105, 36);
            Button sfx = UIEffects.createButton("SFX ON", Color.rgb(170, 210, 235), audioManager, 88, 36);
            start.setOnAction(e -> {
                if (selectedRole == null) {
                    audioManager.playError();
                    showToast("NO SIDE SELECTED", "Choose SCARER or LAUGHER first.", Color.rgb(225, 90, 90));
                    return;
                }

                showMonsterPicker(selectedRole, (chosenMonsterName) -> {
                    onStartGame.accept(new GameConfig(
                            selectedRole,
                            chosenMonsterName,
                            playerNameField.getText(),
                            opponentNameField.getText()
                    ));
                });
            });

            instructions.setOnAction(e -> onInstructions.run());
            fullScreen.setOnAction(e -> onToggleFullScreen.run());
            exit.setOnAction(e -> onExit.run());

            music.setOnAction(e -> {
                audioManager.toggleMusicMute();
                music.setText(audioManager.isMusicMuted() ? "MUSIC OFF" : "MUSIC ON");
            });

            sfx.setOnAction(e -> {
                audioManager.toggleSfxMute();
                sfx.setText(audioManager.isSfxMuted() ? "SFX OFF" : "SFX ON");
            });

            buttons.getChildren().addAll(start, instructions, fullScreen, exit, music, sfx);
            bottom.getChildren().addAll(selectedLabel, names, buttons);

            return bottom;
        }

        private TextField createNameField(String defaultText) {
            TextField field = new TextField(defaultText);
            field.setPrefWidth(180);
            field.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            field.setStyle(
                    "-fx-background-color: rgba(8,14,32,0.95);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-color: rgba(220,235,255,0.45);" +
                            "-fx-padding: 7;"
            );
            return field;
        }

        private VBox nameBox(String title, TextField field, Color color) {
            VBox box = new VBox(4);
            box.setAlignment(Pos.CENTER);

            Label label = new Label(title);
            label.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 10));
            label.setTextFill(color);

            box.getChildren().addAll(label, field);
            return box;
        }

        private void selectRole(String role) {
            selectedRole = role;
            audioManager.playButtonClick();

            if ("SCARER".equals(role)) {
                selectedLabel.setText("SCARER locked. Power path selected.");
                selectedLabel.setTextFill(Color.rgb(145, 185, 250));
                highlightPanel(scarerPanel, Color.rgb(95, 145, 245));
                dimPanel(laugherPanel, Color.rgb(75, 220, 155));
            } else {
                selectedLabel.setText("LAUGHER locked. Speed path selected.");
                selectedLabel.setTextFill(Color.rgb(105, 235, 180));
                highlightPanel(laugherPanel, Color.rgb(75, 220, 155));
                dimPanel(scarerPanel, Color.rgb(95, 145, 245));
            }
        }

        
        private void showMonsterPicker(String role, java.util.function.Consumer<String> onPick) {
            audioManager.playButtonClick();

            String[][] scarerOptions = {
                {"James P. Sullivan", "Dynamo",      "The top scarer. Big swings, doubled energy."},
                {"Randall Boggs",     "Schemer",     "Sneaky and patient. Reduced energy losses."},
                {"Roz",                "Multitasker", "Methodical. +200 on doors. Focus Mode: 2 turns of normal speed."},
                {"Flint",              "Dasher",     "Quick-footed scarer. Double movement."}
            };
            String[][] laugherOptions = {
                {"Yeti",          "Dynamo",      "Banished snow monster. Doubled energy."},
                {"George Sanderson","Schemer",   "Sly and unpredictable. Reduced energy losses."},
                {"Celia Mae",     "Multitasker", "Organized. +200 on doors. Focus Mode: 2 turns of normal speed."},
                {"Mike Wazowski", "Dasher",      "Fast and funny. Double movement."}
            };
            String[][] options = "SCARER".equals(role) ? scarerOptions : laugherOptions;
            Color roleColor = "SCARER".equals(role) ? Color.rgb(95, 145, 245) : Color.rgb(75, 220, 155);

            StackPane overlay = new StackPane();
            overlay.setPickOnBounds(true);
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Rectangle dim = new Rectangle(APP_WIDTH, APP_HEIGHT);
            dim.setFill(Color.rgb(2, 4, 12, 0.78));
            dim.setOpacity(0);

            StackPane card = new StackPane();
            card.setMaxSize(880, 540);
            card.setPrefSize(880, 540);

            Rectangle cardBg = new Rectangle(880, 540);
            cardBg.setArcWidth(34);
            cardBg.setArcHeight(34);
            cardBg.setFill(Color.rgb(8, 12, 30, 0.96));
            cardBg.setStroke(withAlpha(roleColor, 0.85));
            cardBg.setStrokeWidth(2.2);

            DropShadow cardGlow = new DropShadow();
            cardGlow.setColor(withAlpha(roleColor, 0.65));
            cardGlow.setRadius(40);
            cardGlow.setSpread(0.20);
            cardBg.setEffect(cardGlow);

            VBox content = new VBox(18);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(28, 36, 28, 36));

            Label title = new Label("CHOOSE YOUR MONSTER");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 32));
            title.setTextFill(Color.WHITE);
            DropShadow titleGlow = new DropShadow();
            titleGlow.setColor(withAlpha(roleColor, 0.95));
            titleGlow.setRadius(22);
            titleGlow.setSpread(0.22);
            title.setEffect(titleGlow);

            Label subtitle = new Label(role + " — pick which monster (and type) you want to play as.");
            subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            subtitle.setTextFill(Color.rgb(210, 225, 240));

            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(16);
            grid.setAlignment(Pos.CENTER);

            for (int i = 0; i < options.length; i++) {
                String[] opt = options[i];
                StackPane optCard = createMonsterPickerCard(opt[0], opt[1], opt[2], roleColor);
                optCard.setOnMouseClicked(e -> {
                    audioManager.playButtonClick();
                    fadeOutAndRun(overlay, () -> onPick.accept(opt[0]));
                });
                grid.add(optCard, i % 2, i / 2);
            }

            HBox buttons = new HBox(14);
            buttons.setAlignment(Pos.CENTER);

            Button randomBtn = UIEffects.createButton("RANDOM (SURPRISE ME)", Color.rgb(230, 205, 75), audioManager, 230, 40);
            Button cancelBtn = UIEffects.createButton("CANCEL", Color.rgb(225, 105, 105), audioManager, 130, 40);

            randomBtn.setOnAction(e -> {
                audioManager.playButtonClick();
                fadeOutAndRun(overlay, () -> onPick.accept(null));
            });
            cancelBtn.setOnAction(e -> {
                audioManager.playButtonClick();
                fadeOutAndRun(overlay, () -> {});
            });

            buttons.getChildren().addAll(randomBtn, cancelBtn);

            content.getChildren().addAll(title, subtitle, grid, buttons);

            card.getChildren().addAll(cardBg, content);
            overlay.getChildren().addAll(dim, card);

            root.getChildren().add(overlay);

            card.setScaleX(0.85);
            card.setScaleY(0.85);
            card.setOpacity(0);

            FadeTransition dimFade = new FadeTransition(Duration.millis(220), dim);
            dimFade.setToValue(1);

            FadeTransition cardFade = new FadeTransition(Duration.millis(260), card);
            cardFade.setToValue(1);

            ScaleTransition cardScale = new ScaleTransition(Duration.millis(260), card);
            cardScale.setToX(1);
            cardScale.setToY(1);
            cardScale.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(dimFade, cardFade, cardScale).play();
        }

        private StackPane createMonsterPickerCard(String monsterName, String type, String description, Color roleColor) {
            StackPane card = new StackPane();
            card.setPrefSize(380, 150);
            card.setMaxSize(380, 150);

            Color typeColor;
            if ("Dynamo".equals(type)) typeColor = Color.rgb(120, 150, 255);
            else if ("Dasher".equals(type)) typeColor = Color.rgb(95, 255, 170);
            else if ("Multitasker".equals(type)) typeColor = Color.rgb(215, 130, 235);
            else typeColor = Color.rgb(255, 205, 90);

            Rectangle bg = new Rectangle(380, 150);
            bg.setArcWidth(20);
            bg.setArcHeight(20);
            bg.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(14, 20, 42, 0.96)),
                    new Stop(1, Color.rgb(6, 10, 24, 0.98))
            ));
            bg.setStroke(withAlpha(roleColor, 0.72));
            bg.setStrokeWidth(1.6);

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(typeColor, 0.35));
            glow.setRadius(16);
            glow.setSpread(0.10);
            bg.setEffect(glow);

            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14, 18, 14, 18));

            StackPane typeBadge = new StackPane();
            typeBadge.setPrefSize(80, 80);
            typeBadge.setMaxSize(80, 80);

            Circle badgeBg = new Circle(38);
            badgeBg.setFill(withAlpha(typeColor, 0.22));
            badgeBg.setStroke(withAlpha(typeColor, 0.95));
            badgeBg.setStrokeWidth(2.2);

            String pickerIcon;
            if ("Dasher".equals(type)) pickerIcon = "\u26A1";
            else if ("Dynamo".equals(type)) pickerIcon = "\uD83D\uDD0B";
            else if ("Multitasker".equals(type)) pickerIcon = "\u25CE";
            else if ("Schemer".equals(type)) pickerIcon = "\u26D3";
            else pickerIcon = "?";

            Label badgeLabel = new Label(pickerIcon);
            badgeLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 36));
            badgeLabel.setTextFill(Color.WHITE);

            DropShadow badgeGlow = new DropShadow();
            badgeGlow.setColor(withAlpha(typeColor, 0.85));
            badgeGlow.setRadius(14);
            badgeGlow.setSpread(0.30);
            badgeLabel.setEffect(badgeGlow);

            typeBadge.getChildren().addAll(badgeBg, badgeLabel);

            VBox info = new VBox(4);
            info.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label nameLabel = new Label(monsterName);
            nameLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 17));
            nameLabel.setTextFill(Color.WHITE);

            Label typeLabel = new Label(type.toUpperCase());
            typeLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 12));
            typeLabel.setTextFill(typeColor);

            Label descLabel = new Label(description);
            descLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            descLabel.setTextFill(Color.rgb(210, 220, 235));
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(240);

            info.getChildren().addAll(nameLabel, typeLabel, descLabel);
            row.getChildren().addAll(typeBadge, info);

            card.getChildren().addAll(bg, row);

            card.setOnMouseEntered(e -> {
                ScaleTransition s = new ScaleTransition(Duration.millis(120), card);
                s.setToX(1.035);
                s.setToY(1.035);
                s.play();

                DropShadow hoverGlow = new DropShadow();
                hoverGlow.setColor(withAlpha(typeColor, 0.85));
                hoverGlow.setRadius(28);
                hoverGlow.setSpread(0.22);
                bg.setEffect(hoverGlow);
                bg.setStrokeWidth(2.4);
            });
            card.setOnMouseExited(e -> {
                ScaleTransition s = new ScaleTransition(Duration.millis(120), card);
                s.setToX(1.0);
                s.setToY(1.0);
                s.play();

                bg.setEffect(glow);
                bg.setStrokeWidth(1.6);
            });

            card.setStyle("-fx-cursor: hand;");
            return card;
        }

        private void fadeOutAndRun(StackPane overlay, Runnable after) {
            FadeTransition out = new FadeTransition(Duration.millis(200), overlay);
            out.setFromValue(1);
            out.setToValue(0);
            out.setOnFinished(e -> {
                root.getChildren().remove(overlay);
                if (after != null) after.run();
            });
            out.play();
        }

        private void highlightPanel(VBox panel, Color color) {
            panel.setBorder(new Border(new BorderStroke(
                    Color.rgb(255, 255, 255, 1.0),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(3.2)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.95));
            glow.setRadius(58);
            glow.setSpread(0.55);
            panel.setEffect(glow);

            panel.setOpacity(1.0);

            ScaleTransition pulse = new ScaleTransition(Duration.millis(220), panel);
            pulse.setToX(1.025);
            pulse.setToY(1.025);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
        }

        private void resetPanel(VBox panel, Color color) {
            panel.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.72),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(1.8)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.36));
            glow.setRadius(18);
            glow.setSpread(0.10);
            panel.setEffect(glow);
            panel.setOpacity(1.0);
        }

        private void dimPanel(VBox panel, Color color) {
            panel.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.30),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(1.4)
            )));
            panel.setEffect(null);

            FadeTransition ft = new FadeTransition(Duration.millis(260), panel);
            ft.setToValue(0.42);
            ft.play();
        }

        private void showToast(String title, String message, Color color) {
            VBox toast = new VBox(4);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(13, 18, 13, 18));
            toast.setMaxWidth(390);
            toast.setMinWidth(390);

            toast.setBackground(new Background(new BackgroundFill(
                    Color.rgb(7, 12, 30, 0.96),
                    new CornerRadii(18),
                    Insets.EMPTY
            )));

            toast.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.82),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(18),
                    new BorderWidths(1.8)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.52));
            glow.setRadius(21);
            glow.setSpread(0.20);
            toast.setEffect(glow);

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
            titleLabel.setTextFill(Color.WHITE);

            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            messageLabel.setTextFill(Color.rgb(225, 235, 242));
            messageLabel.setWrapText(true);

            toast.getChildren().addAll(titleLabel, messageLabel);

            StackPane.setAlignment(toast, Pos.TOP_RIGHT);
            StackPane.setMargin(toast, new Insets(92, 24, 0, 0));

            toast.setTranslateX(430);
            toast.setOpacity(0);

            root.getChildren().add(toast);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(260), toast);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), toast);
            fadeIn.setToValue(1);

            PauseTransition stay = new PauseTransition(Duration.seconds(1.7));

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(260), toast);
            slideOut.setToX(430);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(260), toast);
            fadeOut.setToValue(0);

            SequentialTransition sequence = new SequentialTransition(
                    new ParallelTransition(slideIn, fadeIn),
                    stay,
                    new ParallelTransition(slideOut, fadeOut)
            );

            sequence.setOnFinished(e -> root.getChildren().remove(toast));
            sequence.play();
        }

        private void playIntroAnimation() {
            scarerPanel.setTranslateX(-80);
            scarerPanel.setOpacity(0);

            laugherPanel.setTranslateX(80);
            laugherPanel.setOpacity(0);

            versusLabel.setScaleX(0.5);
            versusLabel.setScaleY(0.5);
            versusLabel.setOpacity(0);

            TranslateTransition scarerMove = new TranslateTransition(Duration.millis(620), scarerPanel);
            scarerMove.setToX(0);
            scarerMove.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition scarerFade = new FadeTransition(Duration.millis(620), scarerPanel);
            scarerFade.setToValue(1);

            TranslateTransition laugherMove = new TranslateTransition(Duration.millis(620), laugherPanel);
            laugherMove.setToX(0);
            laugherMove.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition laugherFade = new FadeTransition(Duration.millis(620), laugherPanel);
            laugherFade.setToValue(1);

            ScaleTransition vsScale = new ScaleTransition(Duration.millis(520), versusLabel);
            vsScale.setToX(1);
            vsScale.setToY(1);
            vsScale.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition vsFade = new FadeTransition(Duration.millis(520), versusLabel);
            vsFade.setToValue(1);

            SequentialTransition sequence = new SequentialTransition(
                    new PauseTransition(Duration.millis(150)),
                    new ParallelTransition(scarerMove, scarerFade, laugherMove, laugherFade),
                    new ParallelTransition(vsScale, vsFade)
            );

            sequence.play();
        }
    }

    static class InstructionsView {

        private final StackPane root;
        private final Runnable onBack;

        private final StackPane contentHolder = new StackPane();
        private final Map<String, Button> tabButtons = new HashMap<String, Button>();

        private String currentTab = "Objective";

        InstructionsView(Runnable onBack) {
            root = createInstructionsWallpaper();
            this.onBack = onBack;
            build();
        }

        public Parent getRoot() {
            return root;
        }
        private StackPane createInstructionsWallpaper() {
            StackPane sceneRoot = new StackPane();
            sceneRoot.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Image image = null;

            try {
                URL imageUrl = getClass().getResource("/assets/images/start_background.png");

                if (imageUrl != null) {
                    image = new Image(imageUrl.toExternalForm());
                } else {
                    System.out.println("Missing image: /assets/images/start_background.png");
                }
            } catch (Exception e) {
                System.out.println("Could not load instructions background image.");
            }

            if (image == null) {
                return CinematicBackground.createRoot();
            }

            ImageView background = new ImageView(image);
            background.setFitWidth(APP_WIDTH);
            background.setFitHeight(APP_HEIGHT);
            background.setPreserveRatio(false);
            background.setSmooth(true);
            background.setCache(true);

            background.setScaleX(1.0);
            background.setScaleY(1.0);
            background.setTranslateX(0);
            background.setTranslateY(0);

            background.setScaleX(1.04);
            background.setScaleY(1.04);
            background.setTranslateX(0);
            background.setTranslateY(0);

            Rectangle darkOverlay = new Rectangle(APP_WIDTH, APP_HEIGHT);
            darkOverlay.setFill(Color.rgb(0, 0, 0, 0.48));

            Rectangle readabilityOverlay = new Rectangle(APP_WIDTH, APP_HEIGHT);
            readabilityOverlay.setFill(new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.rgb(0, 0, 0, 0.22)),
                    new Stop(0.38, Color.rgb(0, 0, 0, 0.32)),
                    new Stop(0.72, Color.rgb(0, 0, 0, 0.48)),
                    new Stop(1.00, Color.rgb(0, 0, 0, 0.68))
            ));

            Pane particles = new Pane();
            particles.setPrefSize(APP_WIDTH, APP_HEIGHT);
            particles.setMouseTransparent(true);

            Random random = new Random();

           

            Rectangle vignette = createUniversalVignette();

            sceneRoot.getChildren().addAll(
                    background,
                    darkOverlay,
                    readabilityOverlay,
                    vignette
            );

            return sceneRoot;
        }

        private void build() {
            BorderPane main = new BorderPane();
            main.setPadding(new Insets(36, 58, 42, 58));

            VBox header = new VBox(4);
            header.setAlignment(Pos.CENTER);

            Label title = new Label("FACTORY BRIEFING");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 44));
            title.setTextFill(Color.WHITE);
            UIEffects.pulse(title, Color.rgb(105, 220, 255));

            Label subtitle = new Label("Learn the rules before entering Boo's Door.");
            subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            subtitle.setTextFill(Color.rgb(210, 225, 238));

            header.getChildren().addAll(title, subtitle);

            HBox body = new HBox(22);
            body.setAlignment(Pos.CENTER);
            body.setPadding(new Insets(22, 0, 18, 0));

            VBox sidebar = createSidebar();

            contentHolder.setPrefSize(860, 500);
            contentHolder.setMaxSize(860, 500);

            StackPane initialContent = createObjectiveContent();
            contentHolder.getChildren().add(initialContent);

            body.getChildren().addAll(sidebar, contentHolder);

            Button back = UIEffects.createButton("BACK TO START", Color.rgb(105, 190, 245), null, 210, 44);
            back.setOnAction(e -> onBack.run());

            VBox bottom = new VBox(back);
            bottom.setAlignment(Pos.CENTER);

            main.setTop(header);
            main.setCenter(body);
            main.setBottom(bottom);

            root.getChildren().add(main);
        }

        private VBox createSidebar() {
            VBox sidebar = new VBox(13);
            sidebar.setAlignment(Pos.TOP_CENTER);
            sidebar.setPadding(new Insets(18));
            sidebar.setPrefWidth(220);
            sidebar.setMaxWidth(220);

            sidebar.setBackground(new Background(new BackgroundFill(
                    Color.rgb(5, 10, 28, 0.88),
                    new CornerRadii(24),
                    Insets.EMPTY
            )));

            sidebar.setBorder(new Border(new BorderStroke(
                    Color.rgb(125, 205, 235, 0.50),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(24),
                    new BorderWidths(1.6)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(75, 200, 240, 0.30));
            glow.setRadius(18);
            glow.setSpread(0.10);
            sidebar.setEffect(glow);

            Label label = new Label("BRIEFING FILES");
            label.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
            label.setTextFill(Color.rgb(125, 220, 255));

            Button objective = createTabButton("Objective");
            Button turnFlow = createTabButton("Turn Flow");
            Button monsters = createTabButton("Monster Types");
            Button tips = createTabButton("Tips");

            sidebar.getChildren().addAll(label, objective, turnFlow, monsters, tips);

            highlightSelectedTab();

            return sidebar;
        }

        private Button createTabButton(String name) {
            Button button = new Button(name);
            button.setPrefSize(175, 42);
            button.setFocusTraversable(false);
            button.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 12));
            button.setTextFill(Color.WHITE);

            button.setStyle(
                    "-fx-background-color: rgba(12,18,40,0.92);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: rgba(170,220,245,0.35);" +
                            "-fx-border-width: 1.2;" +
                            "-fx-cursor: hand;"
            );

            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(85, 220, 255, 0.25));
            glow.setRadius(10);
            button.setEffect(glow);

            button.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(110), button);
                scale.setToX(1.04);
                scale.setToY(1.04);
                scale.play();
            });

            button.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(110), button);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });

            button.setOnAction(e -> switchTab(name));

            tabButtons.put(name, button);
            return button;
        }

        private void highlightSelectedTab() {
            for (String key : tabButtons.keySet()) {
                Button b = tabButtons.get(key);

                if (key.equals(currentTab)) {
                    b.setStyle(
                            "-fx-background-color: linear-gradient(to right, rgba(20,80,120,0.95), rgba(20,160,190,0.85));" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-color: rgba(255,255,255,0.75);" +
                                    "-fx-border-width: 1.6;" +
                                    "-fx-cursor: hand;"
                    );

                    DropShadow selectedGlow = new DropShadow();
                    selectedGlow.setColor(Color.rgb(85, 220, 255, 0.70));
                    selectedGlow.setRadius(18);
                    selectedGlow.setSpread(0.20);
                    b.setEffect(selectedGlow);
                } else {
                    b.setStyle(
                            "-fx-background-color: rgba(12,18,40,0.92);" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-color: rgba(170,220,245,0.35);" +
                                    "-fx-border-width: 1.2;" +
                                    "-fx-cursor: hand;"
                    );

                    DropShadow glow = new DropShadow();
                    glow.setColor(Color.rgb(85, 220, 255, 0.25));
                    glow.setRadius(10);
                    b.setEffect(glow);
                }
            }
        }

        private void switchTab(String newTab) {
            if (newTab.equals(currentTab)) return;

            Parent oldContent = contentHolder.getChildren().isEmpty() ? null : (Parent) contentHolder.getChildren().get(0);
            StackPane newContent = createContentForTab(newTab);

            newContent.setTranslateX(90);
            newContent.setOpacity(0);

            contentHolder.getChildren().add(newContent);

            if (oldContent != null) {
                TranslateTransition oldSlide = new TranslateTransition(Duration.millis(260), oldContent);
                oldSlide.setToX(-90);
                oldSlide.setInterpolator(Interpolator.EASE_IN);

                FadeTransition oldFade = new FadeTransition(Duration.millis(220), oldContent);
                oldFade.setToValue(0);

                ParallelTransition oldOut = new ParallelTransition(oldSlide, oldFade);
                oldOut.setOnFinished(e -> contentHolder.getChildren().remove(oldContent));
                oldOut.play();
            }

            TranslateTransition newSlide = new TranslateTransition(Duration.millis(320), newContent);
            newSlide.setToX(0);
            newSlide.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition newFade = new FadeTransition(Duration.millis(280), newContent);
            newFade.setToValue(1);

            new ParallelTransition(newSlide, newFade).play();

            currentTab = newTab;
            highlightSelectedTab();
        }

        private StackPane createContentForTab(String tab) {
            if ("Objective".equals(tab)) return createObjectiveContent();
            if ("Turn Flow".equals(tab)) return createTurnFlowContent();
            if ("Monster Types".equals(tab)) return createMonsterTypesContent();
            return createTipsContent();
        }

        private StackPane baseContentPanel(String sectionTitle) {
            StackPane wrapper = new StackPane();
            wrapper.setPrefSize(860, 500);
            wrapper.setMaxSize(860, 500);

            VBox panel = new VBox(18);
            panel.setAlignment(Pos.TOP_CENTER);
            panel.setPadding(new Insets(24));

            panel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(5, 10, 28, 0.90),
                    new CornerRadii(28),
                    Insets.EMPTY
            )));

            panel.setBorder(new Border(new BorderStroke(
                    Color.rgb(125, 205, 235, 0.52),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(1.7)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(70, 190, 230, 0.34));
            glow.setRadius(20);
            glow.setSpread(0.10);
            panel.setEffect(glow);

            Label title = new Label(sectionTitle);
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 25));
            title.setTextFill(Color.WHITE);
            UIEffects.pulse(title, Color.rgb(105, 220, 255));

            panel.getChildren().add(title);
            wrapper.getChildren().add(panel);

            return wrapper;
        }

        private VBox getPanel(StackPane wrapper) {
            return (VBox) wrapper.getChildren().get(0);
        }

        private StackPane createObjectiveContent() {
            StackPane wrapper = baseContentPanel("OBJECTIVE");
            VBox panel = getPanel(wrapper);

            HBox row = new HBox(26);
            row.setAlignment(Pos.CENTER);

            VBox left = new VBox(12);
            left.setAlignment(Pos.CENTER);
            left.setMaxWidth(390);

            Label main = new Label("Reach Boo's Door at cell 99 with at least 1000 energy.");
            main.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 23));
            main.setTextFill(Color.rgb(245, 250, 255));
            main.setWrapText(true);
            main.setTextAlignment(TextAlignment.CENTER);

            Label description = new Label(
                    "You race across a 100-cell factory floor. Doors give or remove energy depending on your role. " +
                            "Cards, conveyors, 2319 hazards, monster cells, shields, freeze, and confusion can change the match."
            );
            description.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            description.setTextFill(Color.rgb(215, 228, 240));
            description.setWrapText(true);
            description.setTextAlignment(TextAlignment.CENTER);

            Label winRule = new Label("Winning Rule: Cell 99 + 1000 Energy");
            winRule.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 18));
            winRule.setTextFill(Color.rgb(255, 215, 90));

            left.getChildren().addAll(main, description, winRule);

            StackPane booDoor = createBooDoorVisual();

            row.getChildren().addAll(left, booDoor);
            panel.getChildren().add(row);

            return wrapper;
        }

        private StackPane createBooDoorVisual() {
            StackPane door = new StackPane();
            door.setPrefSize(280, 360);
            door.setMaxSize(280, 360);

            Color doorColor = Color.rgb(95, 255, 170);

            Rectangle aura = new Rectangle(220, 340);
            aura.setArcWidth(110);
            aura.setArcHeight(110);
            aura.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(doorColor, 0.55)),
                    new Stop(0.6, withAlpha(doorColor, 0.18)),
                    new Stop(1, Color.TRANSPARENT)));
            DropShadow auraGlow = new DropShadow();
            auraGlow.setColor(withAlpha(doorColor, 0.85));
            auraGlow.setRadius(46);
            auraGlow.setSpread(0.30);
            aura.setEffect(auraGlow);

            Rectangle frame = new Rectangle(160, 280);
            frame.setArcWidth(58);
            frame.setArcHeight(58);
            frame.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(20, 38, 56, 0.96)),
                    new Stop(0.5, Color.rgb(10, 22, 38, 0.97)),
                    new Stop(1, Color.rgb(6, 14, 26, 0.98))));
            frame.setStroke(withAlpha(doorColor, 0.95));
            frame.setStrokeWidth(3.4);

            Rectangle innerBorder = new Rectangle(138, 258);
            innerBorder.setArcWidth(46);
            innerBorder.setArcHeight(46);
            innerBorder.setFill(Color.TRANSPARENT);
            innerBorder.setStroke(withAlpha(doorColor, 0.55));
            innerBorder.setStrokeWidth(1.5);

            Rectangle topPanel = new Rectangle(110, 50);
            topPanel.setArcWidth(18);
            topPanel.setArcHeight(18);
            topPanel.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(doorColor, 0.30)),
                    new Stop(1, withAlpha(doorColor, 0.08))));
            topPanel.setStroke(withAlpha(doorColor, 0.65));
            topPanel.setStrokeWidth(1.2);
            StackPane.setAlignment(topPanel, Pos.TOP_CENTER);
            StackPane.setMargin(topPanel, new Insets(60, 0, 0, 0));

            Label numberLabel = new Label("99");
            numberLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 26));
            numberLabel.setTextFill(Color.WHITE);
            DropShadow numberGlow = new DropShadow();
            numberGlow.setColor(withAlpha(doorColor, 0.92));
            numberGlow.setRadius(12);
            numberGlow.setSpread(0.36);
            numberLabel.setEffect(numberGlow);
            StackPane.setAlignment(numberLabel, Pos.TOP_CENTER);
            StackPane.setMargin(numberLabel, new Insets(72, 0, 0, 0));

            Rectangle midPanel = new Rectangle(100, 90);
            midPanel.setArcWidth(14);
            midPanel.setArcHeight(14);
            midPanel.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(doorColor, 0.20)),
                    new Stop(1, Color.rgb(12, 22, 38, 0.94))));
            midPanel.setStroke(withAlpha(doorColor, 0.50));
            midPanel.setStrokeWidth(1.0);
            StackPane.setAlignment(midPanel, Pos.CENTER);
            StackPane.setMargin(midPanel, new Insets(20, 0, 0, 0));

            VBox booText = new VBox(-4);
            booText.setAlignment(Pos.CENTER);
            Label boo = new Label("BOO'S");
            boo.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 22));
            boo.setTextFill(Color.WHITE);
            Label doorWord = new Label("DOOR");
            doorWord.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 22));
            doorWord.setTextFill(Color.WHITE);
            DropShadow textGlow = new DropShadow();
            textGlow.setColor(withAlpha(doorColor, 0.85));
            textGlow.setRadius(14);
            textGlow.setSpread(0.32);
            boo.setEffect(textGlow);
            doorWord.setEffect(textGlow);
            booText.getChildren().addAll(boo, doorWord);
            StackPane.setAlignment(booText, Pos.CENTER);
            StackPane.setMargin(booText, new Insets(28, 0, 0, 0));

            Rectangle handle = new Rectangle(8, 22);
            handle.setArcWidth(6);
            handle.setArcHeight(6);
            handle.setFill(withAlpha(doorColor, 0.95));
            DropShadow handleGlow = new DropShadow();
            handleGlow.setColor(withAlpha(doorColor, 0.85));
            handleGlow.setRadius(8);
            handleGlow.setSpread(0.45);
            handle.setEffect(handleGlow);
            StackPane.setAlignment(handle, Pos.CENTER_RIGHT);
            StackPane.setMargin(handle, new Insets(0, 70, 0, 0));

            Rectangle bottomPanel = new Rectangle(110, 30);
            bottomPanel.setArcWidth(12);
            bottomPanel.setArcHeight(12);
            bottomPanel.setFill(Color.rgb(10, 20, 36, 0.94));
            bottomPanel.setStroke(withAlpha(doorColor, 0.45));
            bottomPanel.setStrokeWidth(1.0);
            StackPane.setAlignment(bottomPanel, Pos.BOTTOM_CENTER);
            StackPane.setMargin(bottomPanel, new Insets(0, 0, 60, 0));

            Label endLabel = new Label("END");
            endLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 13));
            endLabel.setTextFill(withAlpha(doorColor, 0.95));
            StackPane.setAlignment(endLabel, Pos.BOTTOM_CENTER);
            StackPane.setMargin(endLabel, new Insets(0, 0, 67, 0));

            door.getChildren().addAll(aura, frame, innerBorder, topPanel, numberLabel, midPanel, booText, handle, bottomPanel, endLabel);

            ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.7), aura);
            pulse.setFromX(0.92);
            pulse.setFromY(0.96);
            pulse.setToX(1.06);
            pulse.setToY(1.03);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setInterpolator(Interpolator.EASE_BOTH);
            pulse.play();

            FadeTransition flicker = new FadeTransition(Duration.seconds(2.1), handle);
            flicker.setFromValue(0.65);
            flicker.setToValue(1.0);
            flicker.setAutoReverse(true);
            flicker.setCycleCount(Animation.INDEFINITE);
            flicker.setInterpolator(Interpolator.EASE_BOTH);
            flicker.play();

            return door;
        }

        private StackPane createTurnFlowContent() {
            StackPane wrapper = baseContentPanel("TURN FLOW");
            VBox panel = getPanel(wrapper);

            Label hint = new Label("Hover over each phase to see what happens.");
            hint.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            hint.setTextFill(Color.rgb(210, 225, 238));

            HBox flow = new HBox(9);
            flow.setAlignment(Pos.CENTER);

            Label detail = new Label("Each turn follows a cinematic factory sequence.");
            detail.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            detail.setTextFill(Color.rgb(235, 242, 248));
            detail.setWrapText(true);
            detail.setTextAlignment(TextAlignment.CENTER);
            detail.setMaxWidth(720);
            detail.setPadding(new Insets(14));
            detail.setBackground(new Background(new BackgroundFill(
                    Color.rgb(12, 18, 40, 0.80),
                    new CornerRadii(18),
                    Insets.EMPTY
            )));
            detail.setBorder(new Border(new BorderStroke(
                    Color.rgb(125, 205, 235, 0.42),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(18),
                    new BorderWidths(1.2)
            )));

            StackPane n1 = createFlowNode("1", "POWERUP", "Optional: activate a monster powerup if you have enough energy.", Color.rgb(230, 205, 75), detail);
            StackPane n2 = createFlowNode("2", "ROLL", "Roll the dice. Monster type may modify movement.", Color.rgb(105, 220, 255), detail);
            StackPane n3 = createFlowNode("3", "MOVE", "The monster slides cell-by-cell across the factory board.", Color.rgb(95, 255, 170), detail);
            StackPane n4 = createFlowNode("4", "RESOLVE", "The landed cell triggers door, card, conveyor, 2319, monster, or Boo effect.", Color.rgb(215, 130, 235), detail);
            StackPane n5 = createFlowNode("5", "UPDATE", "Stats update, status effects change, and the turn switches.", Color.rgb(255, 145, 105), detail);

            flow.getChildren().addAll(n1, flowLine(), n2, flowLine(), n3, flowLine(), n4, flowLine(), n5);

            panel.getChildren().addAll(hint, flow, detail);

            return wrapper;
        }

        private Rectangle flowLine() {
            Rectangle line = new Rectangle(34, 4);
            line.setArcWidth(6);
            line.setArcHeight(6);
            line.setFill(Color.rgb(105, 220, 255, 0.52));

            FadeTransition pulse = new FadeTransition(Duration.seconds(1.0), line);
            pulse.setFromValue(0.35);
            pulse.setToValue(1.0);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            return line;
        }

        private StackPane createFlowNode(String number, String name, String explanation, Color color, Label detailLabel) {
            StackPane node = new StackPane();
            node.setPrefSize(110, 110);
            node.setMaxSize(110, 110);

            Circle outer = new Circle(50);
            outer.setFill(Color.rgb(8, 14, 32, 0.92));
            outer.setStroke(withAlpha(color, 0.85));
            outer.setStrokeWidth(2.4);

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.54));
            glow.setRadius(18);
            glow.setSpread(0.16);
            outer.setEffect(glow);

            VBox text = new VBox(2);
            text.setAlignment(Pos.CENTER);

            Label numberLabel = new Label(number);
            numberLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 24));
            numberLabel.setTextFill(Color.WHITE);

            Label nameLabel = new Label(name);
            nameLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 10));
            nameLabel.setTextFill(color);

            text.getChildren().addAll(numberLabel, nameLabel);
            node.getChildren().addAll(outer, text);

            node.setOnMouseEntered(e -> {
                detailLabel.setText(name + ": " + explanation);

                ScaleTransition scale = new ScaleTransition(Duration.millis(120), node);
                scale.setToX(1.10);
                scale.setToY(1.10);
                scale.play();

                glow.setRadius(28);
                glow.setSpread(0.25);
            });

            node.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(120), node);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                glow.setRadius(18);
                glow.setSpread(0.16);
            });

            return node;
        }

        private StackPane createMonsterTypesContent() {
            StackPane wrapper = baseContentPanel("MONSTER TYPES");
            VBox panel = getPanel(wrapper);

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(18);
            grid.setVgap(18);

            grid.add(createMonsterTypeCard("Dasher", "Fast movement. Base dice movement is doubled.", "LAUGHER", Color.rgb(95, 255, 170)), 0, 0);
            grid.add(createMonsterTypeCard("Dynamo", "Energy gains and losses are doubled.", "SCARER", Color.rgb(120, 150, 255)), 1, 0);
            grid.add(createMonsterTypeCard("Multitasker", "Moves slower, but gains stronger energy rewards.", "LAUGHER", Color.rgb(215, 130, 235)), 0, 1);
            grid.add(createMonsterTypeCard("Schemer", "Steals, manipulates, and plays around energy rules.", "SCARER", Color.rgb(255, 205, 90)), 1, 1);

            Label hint = new Label("Hover a type card to reveal its passive description.");
            hint.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            hint.setTextFill(Color.rgb(210, 225, 238));

            panel.getChildren().addAll(hint, grid);

            return wrapper;
        }

        private StackPane createTypeBadge(String type, Color color, double size) {
            StackPane badge = new StackPane();
            badge.setPrefSize(size, size);
            badge.setMaxSize(size, size);
            badge.setMinSize(size, size);

            Circle ring = new Circle(size / 2.0);
            ring.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(color, 0.36)),
                    new Stop(1, Color.rgb(8, 14, 30, 0.96))));
            ring.setStroke(withAlpha(color, 0.95));
            ring.setStrokeWidth(2.2);

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.80));
            glow.setRadius(14);
            glow.setSpread(0.32);
            ring.setEffect(glow);

            String icon;
            if ("Dasher".equals(type)) icon = "\u26A1";
            else if ("Dynamo".equals(type)) icon = "\uD83D\uDD0B";
            else if ("Multitasker".equals(type)) icon = "\u25CE";
            else if ("Schemer".equals(type)) icon = "\u26D3";
            else icon = "?";

            Label iconLabel = new Label(icon);
            iconLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, (int) (size * 0.50)));
            iconLabel.setTextFill(Color.WHITE);

            DropShadow iconGlow = new DropShadow();
            iconGlow.setColor(withAlpha(color, 0.85));
            iconGlow.setRadius(10);
            iconGlow.setSpread(0.30);
            iconLabel.setEffect(iconGlow);

            badge.getChildren().addAll(ring, iconLabel);
            return badge;
        }

        private VBox createMonsterTypeCard(String type, String description, String role, Color color) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(14));
            card.setPrefSize(320, 145);

            card.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 14, 32, 0.86),
                    new CornerRadii(22),
                    Insets.EMPTY
            )));

            card.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.65),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(22),
                    new BorderWidths(1.5)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.30));
            glow.setRadius(14);
            glow.setSpread(0.08);
            card.setEffect(glow);

            HBox top = new HBox(12);
            top.setAlignment(Pos.CENTER);

            StackPane mini = createTypeBadge(type, color, 72);

            VBox info = new VBox(4);
            info.setAlignment(Pos.CENTER_LEFT);

            Label typeLabel = new Label(type);
            typeLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 20));
            typeLabel.setTextFill(Color.WHITE);

            Label roleLabel = new Label(role);
            roleLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 11));
            roleLabel.setTextFill(color);

            info.getChildren().addAll(typeLabel, roleLabel);
            top.getChildren().addAll(mini, info);

            Label desc = new Label("Hover to reveal passive...");
            desc.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            desc.setTextFill(Color.rgb(218, 228, 238));
            desc.setWrapText(true);
            desc.setMaxWidth(260);
            desc.setTextAlignment(TextAlignment.CENTER);

            card.getChildren().addAll(top, desc);

            card.setOnMouseEntered(e -> {
                typewriter(desc, description);

                ScaleTransition scale = new ScaleTransition(Duration.millis(120), card);
                scale.setToX(1.035);
                scale.setToY(1.035);
                scale.play();

                glow.setRadius(24);
                glow.setSpread(0.18);
            });

            card.setOnMouseExited(e -> {
                desc.setText("Hover to reveal passive...");

                ScaleTransition scale = new ScaleTransition(Duration.millis(120), card);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                glow.setRadius(14);
                glow.setSpread(0.08);
            });

            return card;
        }

        private void typewriter(Label label, String text) {
            label.setText("");

            final int[] index = {0};

            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), e -> {
                if (index[0] <= text.length()) {
                    label.setText(text.substring(0, index[0]));
                    index[0]++;
                }
            }));

            timeline.setCycleCount(text.length() + 1);
            timeline.play();
        }

        private StackPane createTipsContent() {
            StackPane wrapper = baseContentPanel("PRO TIPS");
            VBox panel = getPanel(wrapper);

            VBox tips = new VBox(14);
            tips.setAlignment(Pos.CENTER);

            tips.getChildren().addAll(
                    createTipCard("Energy is your win condition.", "Reaching cell 99 is not enough. You still need at least 1000 energy.", Color.rgb(255, 215, 90)),
                    createTipCard("Use shields before danger.", "A shield can block negative effects like wrong-door penalties or 2319 hazards.", Color.rgb(105, 220, 255)),
                    createTipCard("Card cells can change everything.", "A single card can swap positions, steal energy, add confusion, or protect you.", Color.rgb(215, 130, 235)),
                    createTipCard("Know your monster type.", "Dasher wins through speed. Dynamo wins through huge energy swings. Schemer wins through manipulation.", Color.rgb(95, 255, 170))
            );

            panel.getChildren().add(tips);
            return wrapper;
        }

        private HBox createTipCard(String title, String body, Color color) {
            HBox card = new HBox(14);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(12, 16, 12, 16));
            card.setMaxWidth(720);

            card.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 14, 32, 0.84),
                    new CornerRadii(18),
                    Insets.EMPTY
            )));

            card.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.55),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(18),
                    new BorderWidths(1.4)
            )));

            Circle dot = new Circle(8);
            dot.setFill(color);

            DropShadow dotGlow = new DropShadow();
            dotGlow.setColor(withAlpha(color, 0.80));
            dotGlow.setRadius(12);
            dotGlow.setSpread(0.18);
            dot.setEffect(dotGlow);

            VBox text = new VBox(3);

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
            titleLabel.setTextFill(Color.WHITE);

            Label bodyLabel = new Label(body);
            bodyLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            bodyLabel.setTextFill(Color.rgb(215, 226, 238));
            bodyLabel.setWrapText(true);
            bodyLabel.setMaxWidth(610);

            text.getChildren().addAll(titleLabel, bodyLabel);
            card.getChildren().addAll(dot, text);

            card.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), card);
                scale.setToX(1.02);
                scale.setToY(1.02);
                scale.play();
            });

            card.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), card);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });

            return card;
        }
    }

    static class GameScreenView {

        private final StackPane root;
        private final AudioManager audioManager;
        private final GameConfig config;
        private final Runnable onBackToStart;

        private MonsterProfile playerMonster;
        private MonsterProfile opponentMonster;

        private String playerDisplayName;
        private String opponentDisplayName;

        private GridPane boardGrid;
        private Label diceLabel;
        private Label actionLabel;
        private Label turnLabel;
        private Label phaseLabel;
        private Label cellPreviewLabel;

        private Label playerNameLabel;
        private Label opponentNameLabel;

        private Label playerEnergyLabel;
        private Label opponentEnergyLabel;
        private Label playerPositionLabel;
        private Label opponentPositionLabel;

        private ProgressBar playerEnergyBar;
        private ProgressBar opponentEnergyBar;

        private Label playerShieldLabel;
        private Label opponentShieldLabel;
        private Label playerConfusionLabel;
        private Label opponentConfusionLabel;
        private Label playerFreezeLabel;
        private Label opponentFreezeLabel;
        private Label playerPowerupLabel;
        private Label opponentPowerupLabel;

        private Label latestCardNameLabel;
        private Label latestCardEffectLabel;

        private TextArea eventLog;

        private int playerPosition = 0;
        private int opponentPosition = 0;
        private int playerEnergy;
        private int opponentEnergy;

        private boolean playerShielded = false;
        private boolean opponentShielded = false;
        private boolean playerFrozen = false;
        private boolean opponentFrozen = false;
        private int playerConfusionTurns = 0;
        private int opponentConfusionTurns = 0;

        private int turnNumber = 1;
        private boolean playerTurn = true;
        private boolean gameOver = false;
        private boolean movementLocked = false;

        private final Random random = new Random();

        private final String[] demoCards = {
                "Position Swap|Swapper|Swap places with opponent if behind.",
                "Contamination Code|Start Over|Player returns to the first cell.",
                "2319 Alert|Start Over|Opponent returns to the first cell.",
                "Small Snatcher|Energy Steal|Steal 50 energy from opponent.",
                "Sneaky Thief|Energy Steal|Steal 100 energy from opponent.",
                "Mega Drain|Energy Steal|Steal 150 energy from opponent.",
                "Super Shield|Shield|Block the next negative energy effect.",
                "Mind Scramble|Confusion|Both monsters are confused for 2 turns.",
                "Total Confusion|Confusion|Both monsters are confused for 3 turns."
        };

        private final Set<Integer> cardCells = new HashSet<Integer>();
        private final Set<Integer> conveyorCells = new HashSet<Integer>();
        private final Set<Integer> sockCells = new HashSet<Integer>();
        private final Set<Integer> monsterCells = new HashSet<Integer>();
        private final Set<Integer> exhaustedDoors = new HashSet<Integer>();
        private final Map<Integer, Integer> doorEnergyValues = new HashMap<Integer, Integer>();

        private final Map<Integer, String> doorRolesByCell = new HashMap<Integer, String>();

        private final Map<Integer, String[]> stationedMonsterByCell = new HashMap<Integer, String[]>();

        private final java.util.List<String[]> cardDeck = new java.util.ArrayList<String[]>();

        private int playerMomentumTurns = 0;
        private int opponentMomentumTurns = 0;
        private int playerFocusModeTurns = 0;
        private int opponentFocusModeTurns = 0;

        private int playerCardsDrawn = 0;
        private int opponentCardsDrawn = 0;
        private final java.util.List<String[]> playerDrawnHistory = new java.util.ArrayList<String[]>();
        private final java.util.List<String[]> opponentDrawnHistory = new java.util.ArrayList<String[]>();
        private final int CARD_PILE_SIZE = 24;

        private VBox playerCardPilePanel;
        private VBox opponentCardPilePanel;
        private Label playerCardPileCountLabel;
        private Label opponentCardPileCountLabel;
        private final java.util.List<StackPane> playerPileCardNodes = new java.util.ArrayList<StackPane>();
        private final java.util.List<StackPane> opponentPileCardNodes = new java.util.ArrayList<StackPane>();

        private Label playerActivePowerupLabel;
        private Label opponentActivePowerupLabel;

        private StackPane playerPortrait;
        private StackPane opponentPortrait;

        GameScreenView(AudioManager audioManager, GameConfig config, Runnable onBackToStart) {
            this.audioManager = audioManager;
            this.config = config;
            this.onBackToStart = onBackToStart;
            this.root = CinematicBackground.createRoot();

            playerDisplayName = config.playerDisplayName;
            opponentDisplayName = config.opponentDisplayName;

            initMonsterProfiles();
            initSpecialCells();

            playerEnergy = playerMonster.startingEnergy;
            opponentEnergy = opponentMonster.startingEnergy;

            build();
            refreshBoard();
            updateStats();

            log("Game initialized. Player chose " + config.chosenRole + ".");
            log(playerDisplayName + " controls " + playerMonster.name + " [" + playerMonster.type + "].");
            log(opponentDisplayName + " controls " + opponentMonster.name + " [" + opponentMonster.type + "].");
            setupCheatKeys();

            PauseTransition intro = new PauseTransition(Duration.millis(350));
            intro.setOnFinished(e -> showMonsterDescription(true, "PLAYER MONSTER READY"));
            
            intro.play();
        }
        

        public Parent getRoot() {
            return root;
        }

        private void initMonsterProfiles() {

            String[][] pool = {
                {"James P. Sullivan","SCARER", "Dynamo",     "The top scarer. Powerful, confident, and built for explosive energy swings.",
                 "Energy Amplification: energy gains and losses are doubled.",
                 "Energy Freeze: freezes the opponent and forces them to skip their next turn.","300"},
                {"Mike Wazowski",    "LAUGHER","Dasher",     "Fast, funny, and hard to catch. Wins through movement pressure.",
                 "Lightning Movement: base dice movement is doubled.",
                 "Momentum Rush: movement becomes 3x for the next 3 turns.","100"},
                {"Randall Boggs",    "SCARER", "Schemer",    "Sneaky, patient, and dangerous. Turns setbacks into advantage.",
                 "Energy Manipulation: energy losses are reduced by 10.",
                 "Chain Attack: steals 100 energy ignoring shields.","20"},
                {"Celia Mae",        "LAUGHER","Multitasker","Organized and quick. Squeezes extra energy from doors.",
                 "Multitasking: gains +200 bonus on door rewards.",
                 "Focus Mode: moves at normal dice speed for 2 turns.","50"},
                {"Roz",              "SCARER", "Multitasker","Always watching. Methodical and relentless.",
                 "Multitasking: gains +200 bonus on door rewards.",
                 "Focus Mode: moves at normal dice speed for 2 turns.","100"},
                {"Yeti",             "LAUGHER","Dynamo",     "Banished snow monster with explosive bursts.",
                 "Energy Amplification: energy gains and losses are doubled.",
                 "Energy Freeze: freezes the opponent and forces them to skip their next turn.","100"},
                {"Flint",            "SCARER", "Dasher",     "Quick-footed scarer who dashes between doors with ease.",
                 "Lightning Movement: base dice movement is doubled.",
                 "Momentum Rush: movement becomes 3x for the next 3 turns.","80"},
                {"George Sanderson", "LAUGHER","Schemer",    "Sly comedian who plays mind games to drain opponents.",
                 "Energy Manipulation: energy losses are reduced by 10.",
                 "Chain Attack: steals 100 energy ignoring shields.","40"}
            };

            java.util.List<String[]> scarers = new java.util.ArrayList<>();
            java.util.List<String[]> laughers = new java.util.ArrayList<>();
            for (String[] m : pool) {
                if ("SCARER".equals(m[1])) scarers.add(m);
                else laughers.add(m);
            }
            java.util.Collections.shuffle(scarers);
            java.util.Collections.shuffle(laughers);

            String[] chosenPlayer = null;
            String[] chosenOpponent;
            if ("SCARER".equals(config.chosenRole)) {

                if (config.chosenMonsterName != null) {
                    for (String[] m : scarers) if (m[0].equals(config.chosenMonsterName)) { chosenPlayer = m; break; }
                }
                if (chosenPlayer == null) chosenPlayer = scarers.get(0);
                chosenOpponent = laughers.get(0);
            } else {
                if (config.chosenMonsterName != null) {
                    for (String[] m : laughers) if (m[0].equals(config.chosenMonsterName)) { chosenPlayer = m; break; }
                }
                if (chosenPlayer == null) chosenPlayer = laughers.get(0);
                chosenOpponent = scarers.get(0);
            }

            playerMonster = new MonsterProfile(
                    chosenPlayer[0], chosenPlayer[1], chosenPlayer[2], chosenPlayer[3],
                    chosenPlayer[4], chosenPlayer[5], Integer.parseInt(chosenPlayer[6])
            );
            opponentMonster = new MonsterProfile(
                    chosenOpponent[0], chosenOpponent[1], chosenOpponent[2], chosenOpponent[3],
                    chosenOpponent[4], chosenOpponent[5], Integer.parseInt(chosenOpponent[6])
            );
        }

        private void initSpecialCells() {

            int[] cards = {4, 12, 28, 36, 48, 56, 60, 76, 86, 90};
            int[] conveyors = {6, 22, 44, 52, 66};
            int[] socks = {32, 42, 74, 84, 98};
            int[] monsters = {2, 18, 34, 54, 82, 88};

            for (int v : cards) cardCells.add(v);
            for (int v : conveyors) conveyorCells.add(v);
            for (int v : socks) sockCells.add(v);
            for (int v : monsters) monsterCells.add(v);

            int[] csvDoorEnergies = {
                120, 90, 110, 95, 105, 100, 115, 85, 100, 110,
                95, 105, 125, 90, 110, 100, 105, 95, 115, 85,
                100, 110, 120, 90, 95, 105, 110, 100, 105, 95,
                115, 85, 100, 110, 125, 90, 110, 100, 105, 95,
                115, 85, 100, 110, 120, 90, 95, 105, 110, 100
            };

            int doorIdx = 0;
            for (int i = 1; i <= 99; i += 2) {
                doorEnergyValues.put(i, csvDoorEnergies[doorIdx % csvDoorEnergies.length]);

                doorRolesByCell.put(i, doorIdx % 2 == 0 ? "SCARER" : "LAUGHER");
                doorIdx++;
            }

            initCardDeck();

            initStationedMonsters();
        }

        private void initCardDeck() {

            String[][] defs = {
                {"Position Swap",       "Swapper",     "Swap places with opponent if behind.",     "4"},
                {"Super Shield",        "Shield",      "Block the next negative energy effect.",   "5"},
                {"Small Snatcher",      "Energy Steal","Steal 50 energy from opponent.",            "3"},
                {"Sneaky Thief",        "Energy Steal","Steal 100 energy from opponent.",           "2"},
                {"Contamination Code",  "Start Over",  "Player returns to the first cell.",         "2"},
                {"2319 Alert",          "Start Over",  "Opponent returns to the first cell.",       "3"},
                {"Mind Scramble",       "Confusion",   "Both monsters are confused for 2 turns.",   "3"},
                {"Total Confusion",     "Confusion",   "Both monsters are confused for 3 turns.",   "2"}
            };
            cardDeck.clear();
            for (String[] def : defs) {
                int rarity = Integer.parseInt(def[3]);
                for (int r = 0; r < rarity; r++) {
                    cardDeck.add(new String[]{def[0], def[1], def[2]});
                }
            }

            cardDeck.add(new String[]{"Reinforced Shield", "Shield", "Block the next negative energy effect."});
            java.util.Collections.shuffle(cardDeck);
        }

        private String[] drawCardFromDeck() {
            if (cardDeck.isEmpty()) initCardDeck();
            return cardDeck.remove(0);
        }

        private void initStationedMonsters() {

            String[][] pool = {
                {"James P. Sullivan", "SCARER",  "Dynamo"},
                {"Mike Wazowski",     "LAUGHER", "Dasher"},
                {"Randall Boggs",     "SCARER",  "Schemer"},
                {"Celia Mae",         "LAUGHER", "Multitasker"},
                {"Roz",               "SCARER",  "Multitasker"},
                {"Yeti",              "LAUGHER", "Dynamo"},
                {"Flint",             "SCARER",  "Dasher"},
                {"George Sanderson",  "LAUGHER", "Schemer"}
            };
            int[] mc = {2, 18, 34, 54, 82, 88};

            java.util.List<String[]> available = new java.util.ArrayList<>();
            for (String[] m : pool) {
                if (!m[0].equals(playerMonster.name) && !m[0].equals(opponentMonster.name)) {
                    available.add(m);
                }
            }
            java.util.Collections.shuffle(available);
            for (int i = 0; i < mc.length && i < available.size(); i++) {
                stationedMonsterByCell.put(mc[i], available.get(i));
            }
        }

        private void build() {
            BorderPane layout = new BorderPane();
            layout.setPadding(new Insets(12));

            layout.setTop(createTopHud());
            layout.setLeft(createMonsterPanel("PLAYER", playerMonster, true));
            layout.setRight(createMonsterPanel("OPPONENT", opponentMonster, false));
            layout.setCenter(createBoardPanel());
            layout.setBottom(createBottomPanel());

            root.getChildren().add(layout);
        }

        private HBox createTopHud() {
            HBox top = new HBox(13);
            top.setAlignment(Pos.CENTER);
            top.setPadding(new Insets(8));
            top.setMaxHeight(74);

            top.setBackground(new Background(new BackgroundFill(
                    Color.rgb(3, 8, 22, 0.92),
                    new CornerRadii(18),
                    Insets.EMPTY
            )));

            top.setBorder(new Border(new BorderStroke(
                    Color.rgb(125, 205, 235, 0.50),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(18),
                    new BorderWidths(1.3)
            )));

            turnLabel = hudLabel("Turn 1 | Player Turn");
            phaseLabel = hudLabel("Phase: POWERUP / ROLL");
            diceLabel = hudLabel("Dice: -");
            actionLabel = hudLabel("Current Action: Game Started");

            top.getChildren().addAll(turnLabel, phaseLabel, diceLabel, actionLabel);
            return top;
        }

        private Label hudLabel(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            label.setTextFill(Color.rgb(238, 245, 250));
            label.setPadding(new Insets(8, 12, 8, 12));

            label.setBackground(new Background(new BackgroundFill(
                    Color.rgb(12, 18, 40, 0.90),
                    new CornerRadii(12),
                    Insets.EMPTY
            )));

            label.setBorder(new Border(new BorderStroke(
                    Color.rgb(190, 225, 245, 0.35),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(12),
                    new BorderWidths(1)
            )));

            label.setMinHeight(40);
            return label;
        }

        private VBox createMonsterPanel(String title, MonsterProfile monster, boolean player) {
            VBox panel = new VBox(4);
            panel.setAlignment(Pos.TOP_CENTER);
            panel.setPadding(new Insets(8));
            panel.setPrefWidth(255);
            panel.setMaxWidth(255);

            Color roleColor = monster.role.equals("SCARER") ? Color.rgb(95, 145, 245) : Color.rgb(75, 220, 155);

            panel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(5, 10, 28, 0.91),
                    new CornerRadii(22),
                    Insets.EMPTY
            )));

            panel.setBorder(new Border(new BorderStroke(
                    withAlpha(roleColor, 0.72),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(22),
                    new BorderWidths(1.8)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(roleColor, 0.36));
            glow.setRadius(14);
            glow.setSpread(0.08);
            panel.setEffect(glow);

            Label titleLabel = UIEffects.sectionTitle(title, roleColor);

            StackPane portrait = createCharacterCutout(monster, roleColor, 120, 105, 90);
            portrait.setOnMouseClicked(e -> showMonsterDescription(player, player ? "PLAYER MONSTER" : "OPPONENT MONSTER"));
            if (player) playerPortrait = portrait; else opponentPortrait = portrait;

            Label clickHint = smallText("Click monster for live details", 9, Color.rgb(190, 205, 215, 0.78));

            Label nameLabel = new Label(monster.name);
            nameLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 15));
            nameLabel.setTextFill(Color.rgb(245, 250, 255));
            nameLabel.setWrapText(true);
            nameLabel.setAlignment(Pos.CENTER);

            Label displayNameLabel = new Label("Controlled by: " + (player ? playerDisplayName : opponentDisplayName));
            displayNameLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 11));
            displayNameLabel.setTextFill(roleColor);
            displayNameLabel.setWrapText(true);
            displayNameLabel.setAlignment(Pos.CENTER);
            displayNameLabel.setOnMouseClicked(e -> editDisplayName(player));

            if (player) {
                playerNameLabel = displayNameLabel;
            } else {
                opponentNameLabel = displayNameLabel;
            }

            Label originalRoleLabel = statLabel("Original Role: " + monster.role);
            Label currentRoleLabel = statLabel("Current Role: " + monster.role);
            Label typeLabel = statLabel("Type: " + monster.type);
            Label passiveLabel = tinyInfo("Passive: " + monster.passive);

            Label energyLabel = statLabel("Energy: " + (player ? playerEnergy : opponentEnergy));
            ProgressBar energyBar = createEnergyBar(player ? playerEnergy : opponentEnergy);

            Label positionLabel = statLabel("Position: 0");

            if (player) {
                playerEnergyLabel = energyLabel;
                playerPositionLabel = positionLabel;
                playerEnergyBar = energyBar;
            } else {
                opponentEnergyLabel = energyLabel;
                opponentPositionLabel = positionLabel;
                opponentEnergyBar = energyBar;
            }

            Label statusTitle = statLabel("Status Effects:");
            Label shield = badge("Shield: None");
            Label confusion = badge("Confusion: None");
            Label freeze = badge("Freeze: No");
            Label powerup = badge("Powerup: Ready");
            Label activePowerup = badge("");
            activePowerup.setVisible(false);
            activePowerup.setManaged(false);

            if (player) {
                playerShieldLabel = shield;
                playerConfusionLabel = confusion;
                playerFreezeLabel = freeze;
                playerPowerupLabel = powerup;
                playerActivePowerupLabel = activePowerup;
            } else {
                opponentShieldLabel = shield;
                opponentConfusionLabel = confusion;
                opponentFreezeLabel = freeze;
                opponentPowerupLabel = powerup;
                opponentActivePowerupLabel = activePowerup;
            }

            panel.getChildren().addAll(
                    titleLabel,
                    portrait,
                    clickHint,
                    nameLabel,
                    displayNameLabel,
                    originalRoleLabel,
                    currentRoleLabel,
                    typeLabel,
                    passiveLabel,
                    energyLabel,
                    energyBar,
                    positionLabel,
                    statusTitle,
                    shield,
                    confusion,
                    freeze,
                    powerup,
                    activePowerup
            );

            VBox pilePanel = createCardPilePanel(player, roleColor);
            if (player) playerCardPilePanel = pilePanel;
            else opponentCardPilePanel = pilePanel;
            panel.getChildren().add(pilePanel);

            if (!player) {
                panel.getChildren().add(createLatestCardPanel());
            }

            return panel;
        }

        private VBox createCardPilePanel(boolean player, Color roleColor) {
            VBox box = new VBox(6);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(8, 6, 8, 6));

            box.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 14, 30, 0.84),
                    new CornerRadii(14),
                    Insets.EMPTY
            )));
            box.setBorder(new Border(new BorderStroke(
                    withAlpha(roleColor, 0.55),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(14),
                    new BorderWidths(1.2)
            )));

            Label title = new Label("CARD PILE");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 10));
            title.setTextFill(roleColor);

            Label countLabel = new Label("0 / " + CARD_PILE_SIZE + " drawn");
            countLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 9));
            countLabel.setTextFill(Color.rgb(200, 215, 230));
            if (player) playerCardPileCountLabel = countLabel;
            else opponentCardPileCountLabel = countLabel;

            StackPane pileStack = new StackPane();
            pileStack.setPrefSize(220, 80);
            pileStack.setMaxSize(220, 80);

            java.util.List<StackPane> nodes = player ? playerPileCardNodes : opponentPileCardNodes;
            nodes.clear();

            int cardW = 32;
            int cardH = 46;
            int totalCards = CARD_PILE_SIZE;

            double xStart = -((totalCards - 1) * 4.0) / 2.0;

            for (int i = 0; i < totalCards; i++) {
                StackPane cardNode = new StackPane();
                cardNode.setPrefSize(cardW, cardH);
                cardNode.setMaxSize(cardW, cardH);

                Rectangle bg = new Rectangle(cardW, cardH);
                bg.setArcWidth(8);
                bg.setArcHeight(8);
                bg.setFill(Color.rgb(30, 36, 56, 0.95));
                bg.setStroke(Color.rgb(70, 80, 110, 0.85));
                bg.setStrokeWidth(1.0);

                Label mark = new Label("?");
                mark.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
                mark.setTextFill(Color.rgb(120, 130, 155));

                cardNode.getChildren().addAll(bg, mark);
                cardNode.setOpacity(0.32);

                cardNode.setTranslateX(xStart + i * 4.0);
                cardNode.setTranslateY((i % 2 == 0 ? -1 : 1));
                cardNode.setRotate((i - totalCards / 2.0) * 1.2);

                nodes.add(cardNode);
                pileStack.getChildren().add(cardNode);
            }

            Label hint = new Label("Click to view history");
            hint.setFont(Font.font("Verdana", FontWeight.BOLD, 8));
            hint.setTextFill(Color.rgb(170, 185, 200, 0.85));

            pileStack.setOnMouseClicked(e -> showDrawnCardHistory(player, roleColor));
            pileStack.setStyle("-fx-cursor: hand;");

            box.getChildren().addAll(title, pileStack, countLabel, hint);
            return box;
        }

        private void updateCardPileVisual(boolean player) {
            Label countLabel = player ? playerCardPileCountLabel : opponentCardPileCountLabel;
            int drawn = player ? playerCardsDrawn : opponentCardsDrawn;
            if (countLabel != null) {
                countLabel.setText(drawn + " / " + CARD_PILE_SIZE + " drawn");
            }
        }

        private String iconForCardType(String cardType) {
            if (cardType == null) return "?";
            if ("Shield".equals(cardType)) return "\uD83D\uDEE1";
            if ("Confusion".equals(cardType)) return "\uD83C\uDF00";
            if ("Energy Steal".equals(cardType)) return "\uD83D\uDCB0";
            if ("Swapper".equals(cardType)) return "\u21C4";
            if ("Start Over".equals(cardType)) return "\u21BB";
            return "?";
        }

        private Color colorForCardType(String cardType) {
            if (cardType == null) return Color.rgb(180, 200, 230);
            if ("Shield".equals(cardType)) return Color.rgb(125, 205, 235);
            if ("Confusion".equals(cardType)) return Color.rgb(215, 130, 235);
            if ("Energy Steal".equals(cardType)) return Color.rgb(230, 205, 75);
            if ("Swapper".equals(cardType)) return Color.rgb(190, 145, 245);
            if ("Start Over".equals(cardType)) return Color.rgb(225, 115, 80);
            return Color.rgb(180, 200, 230);
        }

        private void animateCardDrawFromPile(boolean player, String cardName, String cardType) {
            java.util.List<StackPane> nodes = player ? playerPileCardNodes : opponentPileCardNodes;
            int drawnSoFar = player ? playerCardsDrawn : opponentCardsDrawn;
            if (nodes.isEmpty()) return;

            int idx = Math.min(drawnSoFar - 1, nodes.size() - 1);
            if (idx < 0) idx = 0;
            StackPane targetCard = nodes.get(idx);

            Color typeColor = colorForCardType(cardType);
            String typeIcon = iconForCardType(cardType);

            Rectangle bg = (Rectangle) targetCard.getChildren().get(0);
            Label mark = (Label) targetCard.getChildren().get(1);

            targetCard.toFront();

            Paint brightFill = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(typeColor, 0.95)),
                    new Stop(1, Color.rgb(15, 22, 42, 0.96)));
            Paint brightStroke = withAlpha(typeColor, 0.98);

            Paint settledFill = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(typeColor, 0.55)),
                    new Stop(1, Color.rgb(15, 22, 42, 0.96)));
            Paint settledStroke = withAlpha(typeColor, 0.78);

            bg.setFill(brightFill);
            bg.setStroke(brightStroke);
            bg.setStrokeWidth(1.8);

            DropShadow drawGlow = new DropShadow();
            drawGlow.setColor(withAlpha(typeColor, 0.92));
            drawGlow.setRadius(14);
            drawGlow.setSpread(0.40);
            bg.setEffect(drawGlow);

            mark.setText(typeIcon);
            mark.setTextFill(Color.WHITE);
            mark.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 16));

            FadeTransition brighten = new FadeTransition(Duration.millis(140), targetCard);
            brighten.setFromValue(targetCard.getOpacity());
            brighten.setToValue(1.0);

            ScaleTransition tinyPulse = new ScaleTransition(Duration.millis(180), targetCard);
            tinyPulse.setToX(1.18);
            tinyPulse.setToY(1.18);
            tinyPulse.setAutoReverse(true);
            tinyPulse.setCycleCount(2);

            PauseTransition hold = new PauseTransition(Duration.millis(900));

            FadeTransition dim = new FadeTransition(Duration.millis(420), targetCard);
            dim.setToValue(0.55);

            SequentialTransition seq = new SequentialTransition(
                    new ParallelTransition(brighten, tinyPulse),
                    hold,
                    dim
            );

            seq.setOnFinished(e -> {
                bg.setFill(settledFill);
                bg.setStroke(settledStroke);
                bg.setStrokeWidth(1.2);
                bg.setEffect(null);
                mark.setText(typeIcon);
                mark.setTextFill(withAlpha(typeColor, 0.95));
                mark.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 13));
            });

            seq.play();
        }

        private void showPowerupEffect(boolean player, String effectType) {
            StackPane portrait = player ? playerPortrait : opponentPortrait;
            if (portrait == null) return;

            javafx.geometry.Bounds sceneBounds;
            try {
                sceneBounds = portrait.localToScene(portrait.getBoundsInLocal());
            } catch (Exception ex) {
                return;
            }
            if (sceneBounds == null) return;

            double sceneCx = sceneBounds.getMinX() + sceneBounds.getWidth() / 2.0;
            double sceneCy = sceneBounds.getMinY() + sceneBounds.getHeight() / 2.0;

            javafx.geometry.Point2D localPt = root.sceneToLocal(sceneCx, sceneCy);
            if (localPt == null) return;
            double localCx = localPt.getX();
            double localCy = localPt.getY();

            StackPane effectPane = new StackPane();
            effectPane.setMouseTransparent(true);
            effectPane.setManaged(false);
            effectPane.setPrefSize(140, 140);
            effectPane.setMaxSize(140, 140);
            effectPane.setMinSize(140, 140);

            Color color;
            String iconText;
            switch (effectType) {
                case "SHIELD":
                    color = Color.rgb(125, 205, 235);
                    iconText = "\uD83D\uDEE1";
                    break;
                case "FREEZE":
                    color = Color.rgb(140, 200, 250);
                    iconText = "\u2744";
                    break;
                case "MOMENTUM":
                    color = Color.rgb(75, 220, 155);
                    iconText = "\u26A1";
                    break;
                case "FOCUS":
                    color = Color.rgb(215, 130, 235);
                    iconText = "\u25CE";
                    break;
                case "CHAIN":
                    color = Color.rgb(230, 205, 75);
                    iconText = "\u26D3";
                    break;
                case "CONFUSION":
                    color = Color.rgb(215, 130, 235);
                    iconText = "\uD83C\uDF00";
                    break;
                case "ENERGY_STEAL":
                    color = Color.rgb(230, 205, 75);
                    iconText = "\uD83D\uDCB0";
                    break;
                case "SWAP":
                    color = Color.rgb(190, 145, 245);
                    iconText = "\u21C4";
                    break;
                case "START_OVER":
                    color = Color.rgb(225, 115, 80);
                    iconText = "\u21BB";
                    break;
                default:
                    color = Color.rgb(220, 220, 220);
                    iconText = "\u2728";
            }

            Circle outerRing = new Circle(58);
            outerRing.setFill(Color.TRANSPARENT);
            outerRing.setStroke(withAlpha(color, 0.85));
            outerRing.setStrokeWidth(3.0);

            Circle innerFill = new Circle(48);
            innerFill.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(color, 0.55)),
                    new Stop(1, Color.rgb(0, 0, 0, 0.20))));
            innerFill.setStroke(withAlpha(color, 0.95));
            innerFill.setStrokeWidth(2);

            DropShadow ringGlow = new DropShadow();
            ringGlow.setColor(withAlpha(color, 0.9));
            ringGlow.setRadius(28);
            ringGlow.setSpread(0.55);
            outerRing.setEffect(ringGlow);

            Label iconLabel = new Label(iconText);
            iconLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 52));
            iconLabel.setTextFill(Color.WHITE);
            DropShadow iconGlow = new DropShadow();
            iconGlow.setColor(withAlpha(color, 0.95));
            iconGlow.setRadius(18);
            iconGlow.setSpread(0.45);
            iconLabel.setEffect(iconGlow);

            effectPane.getChildren().addAll(outerRing, innerFill, iconLabel);

            effectPane.setLayoutX(localCx - 70);
            effectPane.setLayoutY(localCy - 70);

            root.getChildren().add(effectPane);

            effectPane.setScaleX(0.2);
            effectPane.setScaleY(0.2);
            effectPane.setOpacity(0);

            ScaleTransition pop = new ScaleTransition(Duration.millis(280), effectPane);
            pop.setToX(1.0);
            pop.setToY(1.0);
            pop.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fi = new FadeTransition(Duration.millis(280), effectPane);
            fi.setToValue(1.0);

            RotateTransition spin = new RotateTransition(Duration.millis(1400), outerRing);
            spin.setByAngle(360);
            spin.setInterpolator(Interpolator.LINEAR);

            ScaleTransition pulse = new ScaleTransition(Duration.millis(420), innerFill);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(0.86);
            pulse.setToY(0.86);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(4);

            PauseTransition hold = new PauseTransition(Duration.millis(950));

            ScaleTransition shrink = new ScaleTransition(Duration.millis(380), effectPane);
            shrink.setToX(0.4);
            shrink.setToY(0.4);
            shrink.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fo = new FadeTransition(Duration.millis(380), effectPane);
            fo.setToValue(0);

            boolean isShield = "SHIELD".equals(effectType);
            if (isShield) {
                RotateTransition shake = new RotateTransition(Duration.millis(70), effectPane);
                shake.setByAngle(15);
                shake.setAutoReverse(true);
                shake.setCycleCount(6);
                shake.setDelay(Duration.millis(700));
                shake.play();
            }

            SequentialTransition seq = new SequentialTransition(
                    new ParallelTransition(pop, fi),
                    hold,
                    new ParallelTransition(shrink, fo)
            );

            spin.play();
            pulse.play();
            seq.setOnFinished(e -> {
                spin.stop();
                pulse.stop();
                root.getChildren().remove(effectPane);
            });
            seq.play();
        }

        private void showDrawnCardHistory(boolean player, Color roleColor) {
            audioManager.playButtonClick();

            java.util.List<String[]> history = player ? playerDrawnHistory : opponentDrawnHistory;
            String who = player ? playerDisplayName : opponentDisplayName;

            StackPane overlay = new StackPane();
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Rectangle dim = new Rectangle(APP_WIDTH, APP_HEIGHT);
            dim.setFill(Color.rgb(2, 4, 12, 0.86));

            VBox card = new VBox(12);
            card.setAlignment(Pos.TOP_CENTER);
            card.setPadding(new Insets(24));
            card.setMaxSize(560, 480);
            card.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 12, 30, 0.97),
                    new CornerRadii(22),
                    Insets.EMPTY
            )));
            card.setBorder(new Border(new BorderStroke(
                    withAlpha(roleColor, 0.85),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(22),
                    new BorderWidths(2)
            )));

            Label title = new Label(who + "'s Drawn Cards");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 22));
            title.setTextFill(Color.WHITE);

            Label subtitle = new Label(history.size() + " card" + (history.size() == 1 ? "" : "s") + " drawn this game");
            subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            subtitle.setTextFill(roleColor);

            VBox list = new VBox(8);
            list.setAlignment(Pos.TOP_LEFT);
            list.setPadding(new Insets(8, 12, 8, 12));

            if (history.isEmpty()) {
                Label empty = new Label("No cards drawn yet.");
                empty.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
                empty.setTextFill(Color.rgb(170, 185, 200));
                list.getChildren().add(empty);
            } else {
                for (int i = 0; i < history.size(); i++) {
                    String[] entry = history.get(i);
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8, 12, 8, 12));
                    row.setBackground(new Background(new BackgroundFill(
                            Color.rgb(14, 20, 40, 0.92),
                            new CornerRadii(10),
                            Insets.EMPTY
                    )));
                    row.setBorder(new Border(new BorderStroke(
                            withAlpha(roleColor, 0.4),
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(10),
                            new BorderWidths(1)
                    )));

                    Label num = new Label("#" + (i + 1));
                    num.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 13));
                    num.setTextFill(roleColor);
                    num.setMinWidth(40);

                    VBox info = new VBox(2);
                    Label cn = new Label(entry[0]);
                    cn.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
                    cn.setTextFill(Color.WHITE);
                    Label cd = new Label(entry[1] + " - " + entry[2]);
                    cd.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
                    cd.setTextFill(Color.rgb(200, 215, 230));
                    cd.setWrapText(true);
                    cd.setMaxWidth(420);
                    info.getChildren().addAll(cn, cd);

                    row.getChildren().addAll(num, info);
                    list.getChildren().add(row);
                }
            }

            ScrollPane scroll = new ScrollPane(list);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(340);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Button close = UIEffects.createButton("CLOSE", roleColor, audioManager, 130, 38);
            close.setOnAction(e -> {
                FadeTransition ft = new FadeTransition(Duration.millis(180), overlay);
                ft.setToValue(0);
                ft.setOnFinished(ev -> root.getChildren().remove(overlay));
                ft.play();
            });

            card.getChildren().addAll(title, subtitle, scroll, close);
            overlay.getChildren().addAll(dim, card);
            overlay.setOpacity(0);

            root.getChildren().add(overlay);

            FadeTransition fi = new FadeTransition(Duration.millis(220), overlay);
            fi.setToValue(1);
            fi.play();
        }

        private void editDisplayName(boolean player) {
            StackPane overlay = new StackPane();
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);
            overlay.setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 0, 0, 0.55),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            Color color = player ? Color.rgb(95, 145, 245) : Color.rgb(75, 220, 155);
            String currentName = player ? playerDisplayName : opponentDisplayName;

            VBox panel = new VBox(16);
            panel.setAlignment(Pos.CENTER);
            panel.setPadding(new Insets(26));
            panel.setMaxSize(440, 260);

            panel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(7, 12, 30, 0.97),
                    new CornerRadii(26),
                    Insets.EMPTY
            )));

            panel.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.82),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(26),
                    new BorderWidths(2)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.55));
            glow.setRadius(30);
            glow.setSpread(0.20);
            panel.setEffect(glow);

            Label title = new Label(player ? "CHANGE PLAYER NAME" : "CHANGE OPPONENT NAME");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 19));
            title.setTextFill(Color.WHITE);

            TextField input = new TextField(currentName);
            input.setPrefWidth(300);
            input.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            input.setStyle(
                    "-fx-background-color: rgba(8,14,32,0.95);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 13;" +
                            "-fx-border-radius: 13;" +
                            "-fx-border-color: rgba(220,235,255,0.50);" +
                            "-fx-padding: 9;"
            );

            HBox buttons = new HBox(14);
            buttons.setAlignment(Pos.CENTER);

            Button save = UIEffects.createButton("SAVE", color, audioManager, 120, 40);
            Button cancel = UIEffects.createButton("CANCEL", Color.rgb(225, 90, 90), audioManager, 120, 40);

            save.setOnAction(e -> {
                String clean = input.getText() == null ? "" : input.getText().trim();

                if (clean.length() == 0) {
                    audioManager.playError();
                    showToastNotification(
                            "INVALID NAME",
                            "Name cannot be empty.",
                            Color.rgb(225, 90, 90),
                            player ? NotifySide.PLAYER : NotifySide.OPPONENT
                    );
                    return;
                }

                if (player) {
                    playerDisplayName = clean;
                } else {
                    opponentDisplayName = clean;
                }

                root.getChildren().remove(overlay);
                updateStats();

                showToastNotification(
                        "NAME UPDATED",
                        (player ? "Player" : "Opponent") + " is now called " + clean + ".",
                        color,
                        player ? NotifySide.PLAYER : NotifySide.OPPONENT
                );
            });

            cancel.setOnAction(e -> root.getChildren().remove(overlay));

            buttons.getChildren().addAll(save, cancel);
            panel.getChildren().addAll(title, input, buttons);

            overlay.getChildren().add(panel);
            root.getChildren().add(overlay);

            panel.setOpacity(0);
            panel.setScaleX(0.82);
            panel.setScaleY(0.82);

            FadeTransition fade = new FadeTransition(Duration.millis(240), panel);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.millis(240), panel);
            scale.setToX(1);
            scale.setToY(1);
            scale.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(fade, scale).play();

            input.requestFocus();
        }

        private Label smallText(String text, int size, Color color) {
            Label label = new Label(text);
            label.setFont(Font.font("Verdana", FontWeight.BOLD, size));
            label.setTextFill(color);
            return label;
        }

        private ProgressBar createEnergyBar(int energy) {
            ProgressBar bar = new ProgressBar(Math.min(1.0, energy / 1000.0));
            bar.setPrefWidth(185);
            bar.setPrefHeight(12);
            bar.setStyle(
                    "-fx-accent: #6ee7b7;" +
                            "-fx-control-inner-background: rgba(10,14,30,0.95);" +
                            "-fx-background-radius: 8;"
            );
            return bar;
        }

        private VBox createLatestCardPanel() {
            VBox cardPanel = new VBox(6);
            cardPanel.setAlignment(Pos.CENTER);
            cardPanel.setPadding(new Insets(8));
            cardPanel.setMaxWidth(205);

            cardPanel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(18, 8, 32, 0.84),
                    new CornerRadii(14),
                    Insets.EMPTY
            )));

            cardPanel.setBorder(new Border(new BorderStroke(
                    Color.rgb(215, 130, 235, 0.62),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(14),
                    new BorderWidths(1.2)
            )));

            Label title = new Label("LATEST CARD");
            title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 11));
            title.setTextFill(Color.rgb(220, 170, 240));

            latestCardNameLabel = new Label("None");
            latestCardNameLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            latestCardNameLabel.setTextFill(Color.WHITE);
            latestCardNameLabel.setWrapText(true);
            latestCardNameLabel.setAlignment(Pos.CENTER);

            latestCardEffectLabel = new Label("No card drawn yet.");
            latestCardEffectLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 10));
            latestCardEffectLabel.setTextFill(Color.rgb(215, 222, 232));
            latestCardEffectLabel.setWrapText(true);
            latestCardEffectLabel.setAlignment(Pos.CENTER);

            cardPanel.getChildren().addAll(title, latestCardNameLabel, latestCardEffectLabel);

            return cardPanel;
        }

        private Label statLabel(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            label.setTextFill(Color.rgb(225, 235, 242));
            label.setWrapText(true);
            label.setMaxWidth(215);
            return label;
        }

        private Label tinyInfo(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Verdana", FontWeight.NORMAL, 9));
            label.setTextFill(Color.rgb(190, 205, 215));
            label.setWrapText(true);
            label.setMaxWidth(215);
            label.setAlignment(Pos.CENTER);
            return label;
        }

        private Label badge(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
            label.setTextFill(Color.rgb(245, 250, 255));
            label.setPadding(new Insets(5, 8, 5, 8));

            label.setBackground(new Background(new BackgroundFill(
                    Color.rgb(18, 25, 52, 0.86),
                    new CornerRadii(10),
                    Insets.EMPTY
            )));

            label.setBorder(new Border(new BorderStroke(
                    Color.rgb(180, 215, 240, 0.34),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1)
            )));

            return label;
        }

        private VBox createBoardPanel() {
            VBox container = new VBox(5);
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(5));

            Label title = UIEffects.sectionTitle("THE FACTORY FLOOR", Color.rgb(105, 220, 255));

            cellPreviewLabel = new Label("Hover over a cell to inspect it.");
            cellPreviewLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            cellPreviewLabel.setTextFill(Color.rgb(220, 235, 245));
            cellPreviewLabel.setPadding(new Insets(5, 12, 5, 12));
            cellPreviewLabel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(5, 10, 28, 0.82),
                    new CornerRadii(10),
                    Insets.EMPTY
            )));
            cellPreviewLabel.setBorder(new Border(new BorderStroke(
                    Color.rgb(125, 205, 235, 0.38),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1)
            )));

            StackPane boardFrame = new StackPane();
            boardFrame.setPadding(new Insets(9));

            boardFrame.setBackground(new Background(new BackgroundFill(
                    Color.rgb(3, 7, 18, 0.76),
                    new CornerRadii(24),
                    Insets.EMPTY
            )));

            boardFrame.setBorder(new Border(new BorderStroke(
                    Color.rgb(125, 205, 235, 0.34),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(24),
                    new BorderWidths(1.6)
            )));

            DropShadow frameGlow = new DropShadow();
            frameGlow.setColor(Color.rgb(70, 180, 220, 0.25));
            frameGlow.setRadius(16);
            frameGlow.setSpread(0.07);
            boardFrame.setEffect(frameGlow);

            boardGrid = new GridPane();
            boardGrid.setAlignment(Pos.CENTER);
            boardGrid.setHgap(4);
            boardGrid.setVgap(4);

            boardFrame.getChildren().add(boardGrid);

            container.getChildren().addAll(title, cellPreviewLabel, boardFrame, createBoardLegend());
            return container;
        }

        private HBox createBoardLegend() {
            HBox legend = new HBox(8);
            legend.setAlignment(Pos.CENTER);
            legend.setPadding(new Insets(4));

            legend.getChildren().addAll(
                    legendBadge("S Door", Color.rgb(95, 145, 245)),
                    legendBadge("L Door", Color.rgb(75, 220, 155)),
                    legendBadge("Card", Color.rgb(215, 130, 235)),
                    legendBadge("Conveyor", Color.rgb(75, 220, 155)),
                    legendBadge("2319", Color.rgb(225, 115, 80)),
                    legendBadge("Monster", Color.rgb(230, 205, 75)),
                    legendBadge("Boo", Color.rgb(230, 145, 230))
            );

            return legend;
        }

        private Label legendBadge(String text, Color color) {
            Label badge = new Label(text);
            badge.setFont(Font.font("Verdana", FontWeight.BOLD, 9));
            badge.setTextFill(Color.WHITE);
            badge.setPadding(new Insets(4, 7, 4, 7));

            badge.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 12, 30, 0.82),
                    new CornerRadii(9),
                    Insets.EMPTY
            )));

            badge.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.62),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(9),
                    new BorderWidths(1)
            )));

            return badge;
        }
        private void styleEnergyBar(ProgressBar bar, int energy) {
            if (bar == null) return;

            if (energy >= 1000) {
                bar.setStyle(
                        "-fx-accent: #FFD700;" +
                                "-fx-control-inner-background: rgba(10,14,30,0.95);" +
                                "-fx-background-radius: 8;"
                );

                DropShadow glow = new DropShadow();
                glow.setColor(Color.rgb(255, 215, 0, 0.85));
                glow.setRadius(16);
                glow.setSpread(0.28);
                bar.setEffect(glow);

            } else if (energy < 100) {
                bar.setStyle(
                        "-fx-accent: #FF3344;" +
                                "-fx-control-inner-background: rgba(10,14,30,0.95);" +
                                "-fx-background-radius: 8;"
                );

                DropShadow glow = new DropShadow();
                glow.setColor(Color.rgb(255, 51, 68, 0.65));
                glow.setRadius(12);
                glow.setSpread(0.18);
                bar.setEffect(glow);

            } else {
                bar.setStyle(
                        "-fx-accent: #6ee7b7;" +
                                "-fx-control-inner-background: rgba(10,14,30,0.95);" +
                                "-fx-background-radius: 8;"
                );

                bar.setEffect(null);
            }
        }

        private VBox createBottomPanel() {
            VBox bottom = new VBox(7);
            bottom.setAlignment(Pos.CENTER);
            bottom.setPadding(new Insets(7));

            HBox actions = new HBox(16);
            actions.setAlignment(Pos.CENTER);

            Button powerup = UIEffects.createButton("ACTIVATE POWERUP", Color.rgb(230, 205, 75), audioManager, 190, 38);
            Button roll = UIEffects.createButton("ROLL DICE", Color.rgb(75, 220, 155), audioManager, 150, 38);
            Button fullScreen = UIEffects.createButton("FULL SCREEN", Color.rgb(215, 130, 235), audioManager, 145, 38);
            Button back = UIEffects.createButton("BACK TO START", Color.rgb(235, 105, 105), audioManager, 170, 38);

            powerup.setOnAction(e -> activatePowerup());
            roll.setOnAction(e -> rollDice());
            fullScreen.setOnAction(e -> {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.setFullScreen(!stage.isFullScreen());
            });
            back.setOnAction(e -> onBackToStart.run());

            actions.getChildren().addAll(powerup, roll, fullScreen, back);
            eventLog = new TextArea();
            eventLog.setEditable(false);
            eventLog.setPrefHeight(76);
            eventLog.setMaxWidth(805);
            eventLog.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            eventLog.setStyle(
                    "-fx-control-inner-background: rgba(4, 8, 20, 0.96);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-color: rgba(4, 8, 20, 0.96);" +
                            "-fx-border-color: rgba(125,205,235,0.40);"
            );

            bottom.getChildren().addAll(actions, eventLog);
            return bottom;
        }
        private void setupCheatKeys() {
            root.setFocusTraversable(true);

            root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (gameOver) return;

                if (event.getCode() == KeyCode.W) {
                    cheatMovePlayerToBooDoor();
                    event.consume();
                } else if (event.getCode() == KeyCode.E) {
                    cheatIncreasePlayerEnergy();
                    event.consume();
                } else if (event.getCode() == KeyCode.J) {
                    cheatActivateConfusion();
                    event.consume();
                }
            });

            root.setOnMouseClicked(e -> root.requestFocus());

            PauseTransition focusDelay = new PauseTransition(Duration.millis(300));
            focusDelay.setOnFinished(e -> root.requestFocus());
            focusDelay.play();
        }

        private void cheatMovePlayerToBooDoor() {
            movementLocked = false;

            playerPosition = 99;

            phaseLabel.setText("Phase: CHEAT MODE");
            actionLabel.setText("Current Action: Cheat W activated. Player moved to Boo's Door.");

            log("CHEAT W: " + playerDisplayName + " was moved directly to cell 99.");

            audioManager.playConveyor();

            showToastNotification(
                    "CHEAT ACTIVATED",
                    "W pressed: " + playerDisplayName + " moved to Boo's Door.",
                    Color.rgb(220, 145, 230),
                    NotifySide.PLAYER
            );

            updateStats();
            refreshBoard();

            PauseTransition checkWin = new PauseTransition(Duration.millis(250));
            checkWin.setOnFinished(e -> handleCellEffect(true));
            checkWin.play();
        }

        private void cheatIncreasePlayerEnergy() {
            movementLocked = false;

            playerEnergy += 1000;

            phaseLabel.setText("Phase: CHEAT MODE");
            actionLabel.setText("Current Action: Cheat E activated. Player energy increased.");

            log("CHEAT E: " + playerDisplayName + " gained +1000 energy.");

            audioManager.playEnergyGain();

            showEnergyPopup("+1000 ENERGY", Color.rgb(75, 220, 155));

            showToastNotification(
                    "CHEAT ACTIVATED",
                    "E pressed: " + playerDisplayName + " gained 1000 energy.",
                    Color.rgb(75, 220, 155),
                    NotifySide.PLAYER
            );

            updateStats();
            refreshBoard();
        }

        private void cheatActivateConfusion() {
            movementLocked = false;

            int duration = 3;
            playerConfusionTurns = Math.max(playerConfusionTurns, duration);
            opponentConfusionTurns = Math.max(opponentConfusionTurns, duration);

            String tmp = playerMonster.role;
            playerMonster.role = opponentMonster.role;
            opponentMonster.role = tmp;

            phaseLabel.setText("Phase: CHEAT MODE");
            actionLabel.setText("Current Action: Cheat J activated. Confusion turn triggered.");

            log("CHEAT J: Confusion activated. Roles swapped. Both monsters confused for " + duration + " turns.");

            audioManager.playCardDraw();

            showToastNotification(
                    "CHEAT ACTIVATED",
                    "J pressed: Roles swapped. Confusion for " + duration + " turns.",
                    Color.rgb(215, 130, 235),
                    NotifySide.SYSTEM
            );

            updateStats();
            refreshBoard();
        }

        private void activatePowerup() {
            if (gameOver || movementLocked) return;

            MonsterProfile current = playerTurn ? playerMonster : opponentMonster;
            MonsterProfile other   = playerTurn ? opponentMonster : playerMonster;
            int currentEnergy = playerTurn ? playerEnergy : opponentEnergy;
            NotifySide side = playerTurn ? NotifySide.PLAYER : NotifySide.OPPONENT;

            if (currentEnergy < 500) {
                audioManager.playError();
                actionLabel.setText("Current Action: Invalid powerup action.");
                phaseLabel.setText("Phase: INVALID ACTION");

                showToastNotification(
                        "INVALID POWERUP",
                        current.name + " needs 500 energy. Current energy: " + currentEnergy,
                        Color.rgb(225, 90, 90),
                        side
                );

                log("Invalid powerup attempt. " + current.name + " needs 500 energy.");
                return;
            }

            if (playerTurn) playerEnergy -= 500; else opponentEnergy -= 500;

            if ("Dynamo".equals(current.type)) {

                if (playerTurn) opponentFrozen = true; else playerFrozen = true;
                log(current.name + " activated Energy Freeze. " + other.name + " is frozen.");
                actionLabel.setText("Current Action: " + current.name + " activated Energy Freeze.");
                showToastNotification("ENERGY FREEZE", other.name + " is frozen for one turn.", Color.rgb(125, 205, 235), side);
                showPowerupEffect(!playerTurn, "FREEZE");

            } else if ("Dasher".equals(current.type)) {

                if (playerTurn) playerMomentumTurns = 3; else opponentMomentumTurns = 3;
                log(current.name + " activated Momentum Rush. 3x movement for 3 turns.");
                actionLabel.setText("Current Action: " + current.name + " activated Momentum Rush.");
                showToastNotification("MOMENTUM RUSH", current.name + " moves 3x faster for 3 turns.", Color.rgb(75, 220, 155), side);
                showPowerupEffect(playerTurn, "MOMENTUM");

            } else if ("Multitasker".equals(current.type)) {

                if (playerTurn) playerFocusModeTurns = 2; else opponentFocusModeTurns = 2;
                log(current.name + " activated Focus Mode. Normal dice speed for 2 turns.");
                actionLabel.setText("Current Action: " + current.name + " activated Focus Mode.");
                showToastNotification("FOCUS MODE", current.name + " moves at full dice speed for 2 turns.", Color.rgb(215, 130, 235), side);
                showPowerupEffect(playerTurn, "FOCUS");

            } else if ("Schemer".equals(current.type)) {

                int stolen = 100;
                if (playerTurn) {
                    int actuallyStolen = Math.min(stolen, opponentEnergy);
                    opponentEnergy -= actuallyStolen;
                    playerEnergy += actuallyStolen;
                } else {
                    int actuallyStolen = Math.min(stolen, playerEnergy);
                    playerEnergy -= actuallyStolen;
                    opponentEnergy += actuallyStolen;
                }
                log(current.name + " activated Chain Attack. Stole " + stolen + " energy from " + other.name + ".");
                actionLabel.setText("Current Action: " + current.name + " activated Chain Attack.");
                showToastNotification("CHAIN ATTACK", current.name + " stole " + stolen + " energy (ignoring shields).", Color.rgb(230, 205, 75), side);
                showPowerupEffect(playerTurn, "CHAIN");
                showEnergyPopup("+" + stolen + " ENERGY", Color.rgb(75, 220, 155));
            }

            phaseLabel.setText("Phase: POWERUP");
            updateStats();
            refreshBoard();
        }

        private void rollDice() {
            if (gameOver || movementLocked) {
                audioManager.playError();
                showToastNotification("ACTION LOCKED", "Wait until the current animation finishes.", Color.rgb(225, 115, 80), NotifySide.SYSTEM);
                return;
            }

            if (playerTurn && playerFrozen) {
                playerFrozen = false;
                log(playerDisplayName + " is frozen and skips this turn.");
                showToastNotification("FROZEN", playerDisplayName + " skips this turn.", Color.rgb(125, 205, 235), NotifySide.PLAYER);
                updateStats();
                completeTurnIfNeeded();
                return;
            }

            if (!playerTurn && opponentFrozen) {
                opponentFrozen = false;
                log(opponentDisplayName + " is frozen and skips this turn.");
                showToastNotification("FROZEN", opponentDisplayName + " skips this turn.", Color.rgb(125, 205, 235), NotifySide.OPPONENT);
                updateStats();
                completeTurnIfNeeded();
                return;
            }

            audioManager.playDiceRoll();

            MonsterProfile currentMonster = playerTurn ? playerMonster : opponentMonster;

            phaseLabel.setText("Phase: DICE ROLL");
            actionLabel.setText("Current Action: " + currentMonster.name + " is rolling...");
            diceLabel.setText("Dice: ?");

            Timeline diceAnimation = new Timeline();

            for (int i = 0; i < 10; i++) {
                diceAnimation.getKeyFrames().add(
                        new KeyFrame(Duration.millis(i * 65), e -> {
                            int rollingNumber = random.nextInt(6) + 1;
                            diceLabel.setText("Dice: " + rollingNumber);
                        })
                );
            }

            diceAnimation.setOnFinished(e -> {
                int dice = random.nextInt(6) + 1;
                int movement = calculateMovement(currentMonster, dice);

                diceLabel.setText("Dice: " + dice + " | Move: " + movement);
                phaseLabel.setText("Phase: MOVEMENT");

                log(currentMonster.name + " rolled " + dice + ". Final movement after type effect: " + movement + ".");

                if (currentMonster.type.equals("Dasher")) {
                    showToastNotification(
                            "DASHER SPEED",
                            currentMonster.name + " doubles movement from " + dice + " to " + movement + ".",
                            Color.rgb(75, 220, 155),
                            playerTurn ? NotifySide.PLAYER : NotifySide.OPPONENT
                    );
                }

                animateMonsterMovement(playerTurn, movement);
            });

            diceAnimation.play();
        }

        private int calculateMovement(MonsterProfile monster, int dice) {
            boolean isPlayer = (monster == playerMonster);
            int momentumTurns = isPlayer ? playerMomentumTurns : opponentMomentumTurns;
            if (monster.type.equals("Dasher")) {
                if (momentumTurns > 0) {
                    if (isPlayer) playerMomentumTurns--; else opponentMomentumTurns--;
                    return dice * 3;
                }
                return dice * 2;
            }
            if (monster.type.equals("Multitasker")) {
                int focusTurns = isPlayer ? playerFocusModeTurns : opponentFocusModeTurns;
                if (focusTurns > 0) {
                    if (isPlayer) playerFocusModeTurns--; else opponentFocusModeTurns--;
                    return dice;
                }
                return Math.max(1, dice / 2);
            }
            return dice;
        }

        private void animateMonsterMovement(boolean player, int steps) {
            movementLocked = true;

            MonsterProfile monster = player ? playerMonster : opponentMonster;

            int start = player ? playerPosition : opponentPosition;
            int target = Math.min(99, start + steps);
            int actualSteps = target - start;

            actionLabel.setText("Current Action: " + monster.name + " sliding from cell " + start + " to " + target);
            log(monster.name + " starts sliding across the board.");

            if (actualSteps <= 0) {
                movementLocked = false;
                handleCellEffect(player);
                completeTurnIfNeeded();
                return;
            }

            int momentumActive = player ? playerMomentumTurns : opponentMomentumTurns;
            boolean isBoost = "Dasher".equals(monster.type) && (momentumActive >= 0) && actualSteps >= 5;

            int stepDuration;
            if (isBoost) stepDuration = 60;
            else if (actualSteps >= 10) stepDuration = 65;
            else if (actualSteps >= 6) stepDuration = 80;
            else stepDuration = 100;

            if (isBoost) {
                triggerRocketBoost(player, actualSteps * stepDuration);
            }

            Timeline movement = new Timeline();

            for (int i = 1; i <= actualSteps; i++) {
                final int stepIdx = i;
                movement.getKeyFrames().add(new KeyFrame(Duration.millis(i * stepDuration), e -> {
                    if (player) playerPosition++;
                    else opponentPosition++;

                    int currentPosition = player ? playerPosition : opponentPosition;
                    actionLabel.setText("Current Action: " + monster.name + " sliding... cell " + currentPosition);

                    boolean isLast = (stepIdx == actualSteps);
                    if (isLast || stepIdx % 3 == 0) refreshBoard();

                    if (isBoost && stepIdx % 3 == 0) audioManager.playDiceRoll();
                }));
            }

            movement.setOnFinished(e -> {
                movementLocked = false;

                int end = player ? playerPosition : opponentPosition;
                log(monster.name + " finished movement at cell " + end + ".");

                handleCellEffect(player);
                completeTurnIfNeeded();
            });

            movement.play();
        }

        private void triggerRocketBoost(boolean player, double duration) {
            HBox trail = new HBox(4);
            trail.setAlignment(Pos.CENTER);
            trail.setMouseTransparent(true);
            trail.setManaged(false);

            Label rocket = new Label("\uD83D\uDE80");
            rocket.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 28));

            Label label = new Label("MOMENTUM RUSH");
            label.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 12));
            label.setTextFill(Color.rgb(75, 220, 155));
            label.setPadding(new Insets(4, 10, 4, 10));
            label.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 14, 30, 0.92),
                    new CornerRadii(10),
                    Insets.EMPTY
            )));
            label.setBorder(new Border(new BorderStroke(
                    Color.rgb(75, 220, 155, 0.85),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1.2)
            )));
            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(75, 220, 155, 0.7));
            glow.setRadius(14);
            glow.setSpread(0.2);
            label.setEffect(glow);

            trail.getChildren().addAll(rocket, label);

            double trailWidth = 240;
            trail.setPrefWidth(trailWidth);
            trail.setLayoutX((APP_WIDTH - trailWidth) / 2.0 - 60);
            trail.setLayoutY(APP_HEIGHT / 2.0 - 220);

            trail.setOpacity(0);
            trail.setScaleX(0.8);
            trail.setScaleY(0.8);

            root.getChildren().add(trail);

            FadeTransition fi = new FadeTransition(Duration.millis(180), trail);
            fi.setToValue(1);

            ScaleTransition si = new ScaleTransition(Duration.millis(220), trail);
            si.setToX(1.0);
            si.setToY(1.0);
            si.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition shoot = new TranslateTransition(Duration.millis(Math.max(280, duration)), trail);
            shoot.setByX(120);
            shoot.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fo = new FadeTransition(Duration.millis(220), trail);
            fo.setToValue(0);
            fo.setDelay(Duration.millis(Math.max(180, duration - 220)));

            ParallelTransition all = new ParallelTransition(fi, si, shoot, fo);
            all.setOnFinished(e -> root.getChildren().remove(trail));
            all.play();
        }

        private void completeTurnIfNeeded() {
            if (gameOver) {
                updateStats();
                refreshBoard();
                return;
            }

            phaseLabel.setText("Phase: TURN SWITCH");

            playerTurn = !playerTurn;

            if (playerTurn) {
                turnNumber++;
            }

            if (playerConfusionTurns > 0) {
                playerConfusionTurns--;
                if (playerConfusionTurns == 0) {

                    playerMonster.role = playerMonster.originalRole;
                }
            }
            if (opponentConfusionTurns > 0) {
                opponentConfusionTurns--;
                if (opponentConfusionTurns == 0) {
                    opponentMonster.role = opponentMonster.originalRole;
                }
            }

            turnLabel.setText("Turn " + turnNumber + " | " + (playerTurn ? "Player Turn" : "Opponent Turn"));

            MonsterProfile next = playerTurn ? playerMonster : opponentMonster;

            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> {
                phaseLabel.setText("Phase: POWERUP / ROLL");
                actionLabel.setText("Current Action: " + next.name + "'s turn.");
            });
            pause.play();

            updateStats();
            refreshBoard();
        }

        private void handleCellEffect(boolean player) {
            int pos = player ? playerPosition : opponentPosition;
            MonsterProfile monster = player ? playerMonster : opponentMonster;
            NotifySide side = player ? NotifySide.PLAYER : NotifySide.OPPONENT;

            phaseLabel.setText("Phase: CELL EFFECT");

            if (pos == 99) {
                int energy = player ? playerEnergy : opponentEnergy;

                if (energy >= 1000) {
                    log(monster.name + " reached Boo's Door with enough energy.");
                    showWinScreen(monster, playerEnergy, opponentEnergy);
                    return;
                } else {
                    int pushedBackTo = 94;

                    if (player) {
                        playerPosition = pushedBackTo;
                    } else {
                        opponentPosition = pushedBackTo;
                    }

                    log(monster.name + " reached Boo's Door without 1000 energy and was pushed back to cell " + pushedBackTo + ".");
                    actionLabel.setText("Current Action: Boo's Door rejected " + monster.name + ".");

                    showToastNotification(
                            "BOO'S DOOR LOCKED",
                            monster.name + " needs 1000 energy. Boo pushed them back to cell " + pushedBackTo + ".",
                            Color.rgb(220, 145, 230),
                            side
                    );

                    updateStats();
                    refreshBoard();
                    return;
                }
            }

            if (cardCells.contains(pos)) {
                audioManager.playCardDraw();

                String[] drawn = drawCardFromDeck();
                String cardName = drawn[0];
                String cardType = drawn[1];
                String cardEffect = drawn[2];

                if (player) {
                    playerCardsDrawn++;
                    playerDrawnHistory.add(new String[]{cardName, cardType, cardEffect});
                } else {
                    opponentCardsDrawn++;
                    opponentDrawnHistory.add(new String[]{cardName, cardType, cardEffect});
                }
                updateCardPileVisual(player);
                animateCardDrawFromPile(player, cardName, cardType);

                applyCardEffect(cardName, cardType, player);

                log(monster.name + " landed on a Card Cell.");
                log("Card drawn: " + cardName + " - " + cardEffect);

                actionLabel.setText("Current Action: " + monster.name + " drew " + cardName + "!");

                if (latestCardNameLabel != null) {
                    latestCardNameLabel.setText(cardName);
                    latestCardEffectLabel.setText(cardEffect);
                }

                showCardReveal(cardName, cardType, cardEffect);

            } else if (conveyorCells.contains(pos)) {
                audioManager.playConveyor();

                int old = pos;
                int boost = 5;

                if (player) {
                    playerPosition = Math.min(99, playerPosition + boost);
                    log(monster.name + " landed on a Conveyor Belt and moved from " + old + " to " + playerPosition + ".");
                } else {
                    opponentPosition = Math.min(99, opponentPosition + boost);
                    log(monster.name + " landed on a Conveyor Belt and moved from " + old + " to " + opponentPosition + ".");
                }

                actionLabel.setText("Current Action: Conveyor Belt activated!");
                showToastNotification("CONVEYOR BOOST", monster.name + " moved forward 5 cells.", Color.rgb(75, 220, 155), side);

            } else if (sockCells.contains(pos)) {
                audioManager.playCdaAlarm();

                if (isShielded(player)) {
                    removeShield(player);
                    log(monster.name + " blocked 2319 contamination with shield.");
                    showToastNotification("SHIELD BLOCKED", monster.name + " blocked the 2319 penalty.", Color.rgb(125, 205, 235), side);
                    audioManager.playShield();
                } else {
                    if (player) {
                        playerEnergy = Math.max(0, playerEnergy - 100);
                        playerPosition = Math.max(0, playerPosition - 4);
                    } else {
                        opponentEnergy = Math.max(0, opponentEnergy - 100);
                        opponentPosition = Math.max(0, opponentPosition - 4);
                    }

                    log(monster.name + " triggered 2319 contamination. Energy -100 and moved backward 4 cells.");
                    actionLabel.setText("Current Action: 2319 contamination!");
                    showToastNotification("2319 CONTAMINATION", monster.name + " lost 100 energy and slid backward.", Color.rgb(225, 115, 80), side);
                    showEnergyPopup("-100 ENERGY", Color.rgb(225, 90, 80));
                }

            } else if (monsterCells.contains(pos)) {
                log(monster.name + " landed on a Monster Cell.");
                actionLabel.setText("Current Action: Monster Cell activated!");
                showMonsterDescription(player, "MONSTER CELL: LIVE MONSTER STATUS");

            } else if (pos % 2 == 1) {
                String doorRole = doorRolesByCell.containsKey(pos) ? doorRolesByCell.get(pos) : "SCARER";
                boolean scarerDoor = "SCARER".equals(doorRole);
                int doorEnergy = doorEnergyValues.containsKey(pos) ? doorEnergyValues.get(pos) : 100;

                if (exhaustedDoors.contains(pos)) {
                    log(monster.name + " landed on an exhausted door. No energy effect.");
                    actionLabel.setText("Current Action: Door already exhausted.");
                    showToastNotification("EXHAUSTED DOOR", "This door has already been used.", Color.rgb(150, 155, 165), side);
                    return;
                }

                if (monster.role.equals(doorRole)) {
                    int gain = applyEnergyGainByType(monster, doorEnergy);

                    if (player) playerEnergy += gain;
                    else opponentEnergy += gain;

                    exhaustedDoors.add(pos);

                    audioManager.playEnergyGain();
                    log(monster.name + " landed on matching " + doorRole + " Door. Energy +" + gain + ".");
                    actionLabel.setText("Current Action: Role match! Energy gained.");
                    showEnergyPopup("+" + gain + " ENERGY", Color.rgb(75, 220, 155));
                    showToastNotification("ROLE MATCH", monster.name + " collected " + gain + " energy.", Color.rgb(75, 220, 155), side);

                } else {
                    int loss = applyEnergyLossByType(monster, doorEnergy);

                    if (isShielded(player)) {
                        removeShield(player);
                        log(monster.name + " blocked wrong-door penalty with shield.");
                        showToastNotification("SHIELD BLOCKED", monster.name + " blocked the wrong-door penalty.", Color.rgb(125, 205, 235), side);
                        audioManager.playShield();
                    } else {
                        if (player) playerEnergy = Math.max(0, playerEnergy - loss);
                        else opponentEnergy = Math.max(0, opponentEnergy - loss);

                        audioManager.playEnergyLoss();
                        log(monster.name + " landed on wrong " + doorRole + " Door. Energy -" + loss + ".");
                        actionLabel.setText("Current Action: Wrong door! Energy lost.");
                        showEnergyPopup("-" + loss + " ENERGY", Color.rgb(225, 90, 90));
                        showToastNotification("WRONG DOOR", monster.name + " lost " + loss + " energy.", Color.rgb(225, 90, 90), side);
                    }

                    exhaustedDoors.add(pos);
                }

            } else {
                log(monster.name + " landed on a Normal Cell.");
                actionLabel.setText("Current Action: " + monster.name + " landed on a normal factory tile.");
            }

            updateStats();
            refreshBoard();
        }

        private void applyCardEffect(String cardName, String cardType, boolean player) {
            NotifySide side = player ? NotifySide.PLAYER : NotifySide.OPPONENT;

            if ("Shield".equals(cardType)) {
                if (player) playerShielded = true;
                else opponentShielded = true;

                audioManager.playShield();
                showToastNotification("SHIELD READY", cardName + " activated. Next negative effect will be blocked.", Color.rgb(125, 205, 235), side);
                showPowerupEffect(player, "SHIELD");

            } else if ("Confusion".equals(cardType)) {
                int duration = cardName.contains("Total") ? 3 : 2;
                playerConfusionTurns = Math.max(playerConfusionTurns, duration);
                opponentConfusionTurns = Math.max(opponentConfusionTurns, duration);

                String tmp = playerMonster.role;
                playerMonster.role = opponentMonster.role;
                opponentMonster.role = tmp;

                showToastNotification("CONFUSION", "Both monsters had their roles swapped for " + duration + " turns.", Color.rgb(215, 130, 235), NotifySide.SYSTEM);
                showPowerupEffect(true, "CONFUSION");
                showPowerupEffect(false, "CONFUSION");

            } else if ("Swapper".equals(cardType)) {

                int drawerPos = player ? playerPosition : opponentPosition;
                int otherPos  = player ? opponentPosition : playerPosition;
                if (drawerPos < otherPos) {
                    if (player) { playerPosition = otherPos; opponentPosition = drawerPos; }
                    else        { opponentPosition = otherPos; playerPosition = drawerPos; }
                    showToastNotification("POSITION SWAP", cardName + ": positions swapped.", Color.rgb(215, 130, 235), side);
                    showPowerupEffect(player, "SWAP");
                } else {
                    showToastNotification("POSITION SWAP", "No swap (you are ahead).", Color.rgb(160, 160, 175), side);
                }

            } else if ("Start Over".equals(cardType)) {
                boolean opponentReturns = cardName.contains("2319");
                if (opponentReturns) {
                    if (player) opponentPosition = 0; else playerPosition = 0;
                    showToastNotification("START OVER", cardName + ": opponent returns to cell 0.", Color.rgb(225, 115, 80), side);
                    showPowerupEffect(!player, "START_OVER");
                } else {
                    if (player) playerPosition = 0; else opponentPosition = 0;
                    showToastNotification("START OVER", cardName + ": you return to cell 0.", Color.rgb(225, 115, 80), side);
                    showPowerupEffect(player, "START_OVER");
                }

            } else if ("Energy Steal".equals(cardType)) {
                int amount = 50;
                if (cardName.contains("Sneaky")) amount = 100;
                else if (cardName.contains("Mega")) amount = 150;

                boolean victimShielded = player ? opponentShielded : playerShielded;
                if (victimShielded) {
                    if (player) opponentShielded = false; else playerShielded = false;
                    showToastNotification("SHIELD BLOCKED", "Energy steal was blocked by shield.", Color.rgb(125, 205, 235), side);
                    showPowerupEffect(!player, "SHIELD");
                    audioManager.playShield();
                } else {
                    if (player) {
                        opponentEnergy = Math.max(0, opponentEnergy - amount);
                        playerEnergy += amount;
                    } else {
                        playerEnergy = Math.max(0, playerEnergy - amount);
                        opponentEnergy += amount;
                    }
                    audioManager.playEnergyGain();
                    showToastNotification("ENERGY STEAL", cardName + ": stole " + amount + " energy.", Color.rgb(230, 205, 75), side);
                    showPowerupEffect(player, "ENERGY_STEAL");
                    showEnergyPopup("+" + amount + " ENERGY", Color.rgb(75, 220, 155));
                }
            }
        }

        private boolean isShielded(boolean player) {
            return player ? playerShielded : opponentShielded;
        }

        private void removeShield(boolean player) {
            if (player) playerShielded = false;
            else opponentShielded = false;
        }

        private int applyEnergyGainByType(MonsterProfile monster, int amount) {
            if (monster.type.equals("Dynamo")) return amount * 2;
            if (monster.type.equals("Multitasker")) return amount + 200;
            if (monster.type.equals("Schemer")) return amount + 10;
            return amount;
        }

        private int applyEnergyLossByType(MonsterProfile monster, int amount) {
            if (monster.type.equals("Dynamo")) return amount * 2;
            if (monster.type.equals("Multitasker")) return amount + 200;
            if (monster.type.equals("Schemer")) return Math.max(0, amount - 10);
            return amount;
        }

        private void updateStats() {
            playerEnergyLabel.setText("Energy: " + playerEnergy);
            opponentEnergyLabel.setText("Energy: " + opponentEnergy);
            playerPositionLabel.setText("Position: " + playerPosition);
            opponentPositionLabel.setText("Position: " + opponentPosition);

            String pRoleText = playerMonster.role.equals(playerMonster.originalRole)
                    ? playerMonster.originalRole
                    : playerMonster.originalRole + " → " + playerMonster.role + " (CONFUSED)";
            String oRoleText = opponentMonster.role.equals(opponentMonster.originalRole)
                    ? opponentMonster.originalRole
                    : opponentMonster.originalRole + " → " + opponentMonster.role + " (CONFUSED)";

            playerNameLabel.setText("Controlled by: " + playerDisplayName + " | " + pRoleText);
            opponentNameLabel.setText("Controlled by: " + opponentDisplayName + " | " + oRoleText);

            if (playerEnergyBar != null) playerEnergyBar.setProgress(Math.min(1.0, playerEnergy / 1000.0));
            if (opponentEnergyBar != null) opponentEnergyBar.setProgress(Math.min(1.0, opponentEnergy / 1000.0));
            styleEnergyBar(playerEnergyBar, playerEnergy);
            styleEnergyBar(opponentEnergyBar, opponentEnergy);

            playerShieldLabel.setText("Shield: " + (playerShielded ? "Yes" : "None"));
            opponentShieldLabel.setText("Shield: " + (opponentShielded ? "Yes" : "None"));

            playerFreezeLabel.setText("Freeze: " + (playerFrozen ? "Yes" : "No"));
            opponentFreezeLabel.setText("Freeze: " + (opponentFrozen ? "Yes" : "No"));

            playerConfusionLabel.setText("Confusion: " + (playerConfusionTurns > 0 ? playerConfusionTurns + " turns" : "None"));
            opponentConfusionLabel.setText("Confusion: " + (opponentConfusionTurns > 0 ? opponentConfusionTurns + " turns" : "None"));

            styleStatusBadge(playerShieldLabel, playerShielded, Color.rgb(125, 205, 235));
            styleStatusBadge(opponentShieldLabel, opponentShielded, Color.rgb(125, 205, 235));
            styleStatusBadge(playerFreezeLabel, playerFrozen, Color.rgb(125, 205, 235));
            styleStatusBadge(opponentFreezeLabel, opponentFrozen, Color.rgb(125, 205, 235));
            styleStatusBadge(playerConfusionLabel, playerConfusionTurns > 0, Color.rgb(215, 130, 235));
            styleStatusBadge(opponentConfusionLabel, opponentConfusionTurns > 0, Color.rgb(215, 130, 235));

            updateActivePowerupLabel(true);
            updateActivePowerupLabel(false);
        }

        private void updateActivePowerupLabel(boolean player) {
            Label lbl = player ? playerActivePowerupLabel : opponentActivePowerupLabel;
            if (lbl == null) return;

            int momentum = player ? playerMomentumTurns : opponentMomentumTurns;
            int focus = player ? playerFocusModeTurns : opponentFocusModeTurns;

            String text = null;
            Color color = Color.rgb(230, 205, 75);

            if (momentum > 0) {
                text = "⚡ Momentum Rush: " + momentum + " turn" + (momentum == 1 ? "" : "s") + " left";
                color = Color.rgb(75, 220, 155);
            } else if (focus > 0) {
                text = "◎ Focus Mode: " + focus + " turn" + (focus == 1 ? "" : "s") + " left";
                color = Color.rgb(215, 130, 235);
            }

            if (text == null) {
                lbl.setVisible(false);
                lbl.setManaged(false);
            } else {
                lbl.setText(text);
                lbl.setVisible(true);
                lbl.setManaged(true);
                styleStatusBadge(lbl, true, color);
                lbl.setTextFill(color);
            }
        }

        private void styleStatusBadge(Label label, boolean active, Color color) {
            if (!active) {
                label.setBorder(new Border(new BorderStroke(
                        Color.rgb(180, 215, 240, 0.34),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(10),
                        new BorderWidths(1)
                )));
                label.setEffect(null);
                return;
            }

            label.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.85),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1.7)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.55));
            glow.setRadius(12);
            glow.setSpread(0.18);
            label.setEffect(glow);
        }

        private void refreshBoard() {
            boardGrid.getChildren().clear();

            for (int displayRow = 0; displayRow < 10; displayRow++) {
                for (int col = 0; col < 10; col++) {
                    int actualRowFromBottom = 9 - displayRow;
                    int base = actualRowFromBottom * 10;

                    int index;

                    if (actualRowFromBottom % 2 == 0) index = base + col;
                    else index = base + (9 - col);

                    StackPane cell = createCell(index);
                    boardGrid.add(cell, col, displayRow);
                }
            }
        }

        private StackPane createCell(int index) {
            StackPane cell = new StackPane();
            cell.setPrefSize(58, 39);
            cell.setMaxSize(58, 39);

            boolean occupied = index == playerPosition || index == opponentPosition;
            boolean exhausted = exhaustedDoors.contains(index);

            Color fill;
            Color border;
            String mainText;
            String subText;
            String details;

            if (index == 99) {
                fill = Color.rgb(115, 55, 135, 0.92);
                border = Color.rgb(220, 145, 230);
                mainText = "BOO";
                subText = "END";
                details = "Boo's Door | Final cell | Need 1000 energy";
            } else if (cardCells.contains(index)) {
                fill = Color.rgb(75, 35, 105, 0.92);
                border = Color.rgb(215, 130, 235);
                mainText = "CARD";
                subText = "◆";
                details = "Card Cell | Draws a mysterious card";
            } else if (conveyorCells.contains(index)) {
                fill = Color.rgb(22, 82, 62, 0.92);
                border = Color.rgb(75, 220, 155);
                mainText = "MOVE";
                subText = ">>";
                details = "Conveyor Belt | Moves monster forward";
            } else if (sockCells.contains(index)) {
                fill = Color.rgb(96, 44, 24, 0.92);
                border = Color.rgb(225, 115, 80);
                mainText = "2319";
                subText = "!";
                details = "Contamination Sock | Moves backward and drains energy";
            } else if (monsterCells.contains(index)) {
                fill = Color.rgb(85, 70, 25, 0.92);
                border = Color.rgb(230, 205, 75);
                String[] stationed = stationedMonsterByCell.get(index);
                if (stationed != null) {
                    String shortName = stationed[0].length() > 6 ? stationed[0].substring(0, 6) : stationed[0];
                    mainText = shortName;
                    subText = "★ " + stationed[2].substring(0, Math.min(3, stationed[2].length())).toUpperCase();
                    details = "Monster Cell | " + stationed[0] + " (" + stationed[1] + " " + stationed[2] + ")";
                } else {
                    mainText = "MON";
                    subText = "★";
                    details = "Monster Cell | Stationed monster interaction";
                }
            } else if (index % 2 == 1) {
                String doorRole = doorRolesByCell.containsKey(index) ? doorRolesByCell.get(index) : "SCARER";
                boolean scarerDoor = "SCARER".equals(doorRole);
                int energy = doorEnergyValues.containsKey(index) ? doorEnergyValues.get(index) : 100;

                if (scarerDoor) {
                    fill = exhausted ? Color.rgb(36, 41, 52, 0.88) : Color.rgb(28, 54, 112, 0.92);
                    border = exhausted ? Color.rgb(88, 94, 105) : Color.rgb(95, 145, 245);
                    mainText = exhausted ? "USED" : "S";
                    subText = exhausted ? "--" : String.valueOf(energy);
                    details = "SCARER Door | Energy: " + energy + (exhausted ? " | Exhausted" : " | Active");
                } else {
                    fill = exhausted ? Color.rgb(36, 41, 52, 0.88) : Color.rgb(24, 86, 67, 0.92);
                    border = exhausted ? Color.rgb(88, 94, 105) : Color.rgb(75, 220, 155);
                    mainText = exhausted ? "USED" : "L";
                    subText = exhausted ? "--" : String.valueOf(energy);
                    details = "LAUGHER Door | Energy: " + energy + (exhausted ? " | Exhausted" : " | Active");
                }
            } else {
                fill = Color.rgb(18, 24, 40, 0.92);
                border = Color.rgb(120, 155, 180, 0.36);
                mainText = "";
                subText = "";
                details = "Normal Cell | Factory metal tile";
            }

            Rectangle bg = new Rectangle(58, 39);
            bg.setArcWidth(9);
            bg.setArcHeight(9);
            bg.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, fill.brighter()),
                    new Stop(0.52, fill),
                    new Stop(1, fill.darker())
            ));
            bg.setStroke(border);
            bg.setStrokeWidth(occupied ? 2.1 : 1.1);

            Rectangle inner = new Rectangle(48, 29);
            inner.setArcWidth(7);
            inner.setArcHeight(7);
            inner.setFill(Color.rgb(255, 255, 255, 0.025));
            inner.setStroke(Color.rgb(255, 255, 255, 0.08));
            inner.setStrokeWidth(0.7);

            VBox content = new VBox(0);
            content.setAlignment(Pos.CENTER);

            Label indexLabel = new Label(String.valueOf(index));
            indexLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 8));
            indexLabel.setTextFill(Color.rgb(245, 250, 255, 0.75));

            Label mainLabel = new Label(mainText);
            mainLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 8.2));
            mainLabel.setTextFill(exhausted ? Color.rgb(155, 160, 165) : Color.rgb(245, 250, 255));

            Label subLabel = new Label(subText);
            subLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 7.1));
            subLabel.setTextFill(exhausted ? Color.rgb(135, 140, 145) : Color.rgb(210, 230, 245));

            HBox tokens = new HBox(1);
            tokens.setAlignment(Pos.CENTER);

            if (playerPosition == index) {
                tokens.getChildren().add(MonsterArt.createSmallMonsterToken(playerMonster.role, playerMonster.type, true));
            }

            if (opponentPosition == index) {
                tokens.getChildren().add(MonsterArt.createSmallMonsterToken(opponentMonster.role, opponentMonster.type, false));
            }

            content.getChildren().addAll(indexLabel, mainLabel, subLabel, tokens);
            cell.getChildren().addAll(bg, inner, content);

            cell.setOnMouseEntered(e -> {
                cellPreviewLabel.setText("Cell " + index + " | " + details);
                bg.setStrokeWidth(2.4);

                DropShadow hoverGlow = new DropShadow();
                hoverGlow.setColor(withAlpha(border, 0.58));
                hoverGlow.setRadius(13);
                hoverGlow.setSpread(0.14);
                cell.setEffect(hoverGlow);

                ScaleTransition scale = new ScaleTransition(Duration.millis(90), cell);
                scale.setToX(1.050);
                scale.setToY(1.050);
                scale.play();
            });

            cell.setOnMouseExited(e -> {
                cellPreviewLabel.setText("Hover over a cell to inspect it.");
                bg.setStrokeWidth(occupied ? 2.1 : 1.1);

                if (occupied || index == 99) {
                    DropShadow glow = new DropShadow();
                    glow.setColor(withAlpha(border, 0.52));
                    glow.setRadius(11);
                    glow.setSpread(0.14);
                    cell.setEffect(glow);
                } else {
                    cell.setEffect(null);
                }

                ScaleTransition scale = new ScaleTransition(Duration.millis(90), cell);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });

            if (occupied || index == 99) {
                DropShadow glow = new DropShadow();
                glow.setColor(withAlpha(border, 0.52));
                glow.setRadius(11);
                glow.setSpread(0.14);
                cell.setEffect(glow);
            }

            return cell;
        }

        private void showToastNotification(String title, String message, Color color, NotifySide side) {
            HBox toast = new HBox(10);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(8, 14, 8, 12));
            toast.setMouseTransparent(true);
            toast.setManaged(false);

            toast.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 14, 30, 0.96),
                    new CornerRadii(10),
                    Insets.EMPTY
            )));

            toast.setBorder(new Border(new BorderStroke(
                    withAlpha(color, 0.72),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1.2)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.35));
            glow.setRadius(10);
            glow.setSpread(0.06);
            toast.setEffect(glow);

            Rectangle accent = new Rectangle(3, 22);
            accent.setArcWidth(3);
            accent.setArcHeight(3);
            accent.setFill(color);

            VBox text = new VBox(1);

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 11));
            titleLabel.setTextFill(color);
            titleLabel.setMinWidth(Region.USE_PREF_SIZE);
            titleLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
            messageLabel.setTextFill(Color.rgb(225, 235, 242));
            messageLabel.setWrapText(false);
            messageLabel.setMinWidth(Region.USE_PREF_SIZE);
            messageLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

            text.getChildren().addAll(titleLabel, messageLabel);
            toast.getChildren().addAll(accent, text);

            double toastWidth = 420;
            double toastHeight = 52;
            toast.setPrefSize(toastWidth, toastHeight);
            toast.setMinSize(toastWidth, toastHeight);
            toast.setMaxSize(toastWidth, toastHeight);
            toast.resize(toastWidth, toastHeight);

            double toastX = (APP_WIDTH - toastWidth) / 2.0;
            double toastY = 78;
            toast.setLayoutX(toastX);
            toast.setLayoutY(toastY);

            toast.setTranslateY(-10);
            toast.setOpacity(0);
            root.getChildren().add(toast);

            toast.applyCss();
            toast.layout();

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), toast);
            slideIn.setToY(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
            fadeIn.setToValue(1);

            PauseTransition stay = new PauseTransition(Duration.millis(1500));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
            fadeOut.setToValue(0);

            SequentialTransition sequence = new SequentialTransition(
                    new ParallelTransition(slideIn, fadeIn),
                    stay,
                    fadeOut
            );

            sequence.setOnFinished(e -> root.getChildren().remove(toast));
            sequence.play();
        }

        private void showMonsterDescription(boolean player, String heading) {
            MonsterProfile monster = player ? playerMonster : opponentMonster;
            String displayName = player ? playerDisplayName : opponentDisplayName;
            int energy = player ? playerEnergy : opponentEnergy;
            int position = player ? playerPosition : opponentPosition;
            boolean shield = player ? playerShielded : opponentShielded;
            boolean frozen = player ? playerFrozen : opponentFrozen;
            int confusion = player ? playerConfusionTurns : opponentConfusionTurns;

            StackPane overlay = new StackPane();
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);
            overlay.setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 0, 0, 0.58),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            HBox panel = new HBox(24);
            panel.setAlignment(Pos.CENTER);
            panel.setPadding(new Insets(26));
            panel.setMaxSize(820, 430);

            Color roleColor = monster.role.equals("SCARER") ? Color.rgb(95, 145, 245) : Color.rgb(75, 220, 155);

            panel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(6, 11, 30, 0.97),
                    new CornerRadii(28),
                    Insets.EMPTY
            )));

            panel.setBorder(new Border(new BorderStroke(
                    withAlpha(roleColor, 0.82),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(2.2)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(roleColor, 0.56));
            glow.setRadius(30);
            glow.setSpread(0.20);
            panel.setEffect(glow);

            StackPane art = createCharacterCutout(monster, roleColor, 210, 190, 165);

            VBox info = new VBox(10);
            info.setAlignment(Pos.CENTER_LEFT);
            info.setMaxWidth(480);

            Label headingLabel = new Label(heading);
            headingLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 15));
            headingLabel.setTextFill(roleColor);

            Label name = new Label(monster.name);
            name.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 32));
            name.setTextFill(Color.WHITE);
            name.setWrapText(true);

            Label controlled = new Label("Controlled by: " + displayName);
            controlled.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 14));
            controlled.setTextFill(roleColor);

            Label meta = new Label(monster.role + "  •  " + monster.type + "  •  Position: " + position + "  •  Energy: " + energy);
            meta.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            meta.setTextFill(Color.rgb(220, 230, 240));

            Label personality = new Label(monster.personality);
            personality.setFont(Font.font("Verdana", FontWeight.NORMAL, 13));
            personality.setTextFill(Color.rgb(220, 230, 240));
            personality.setWrapText(true);

            Label passive = new Label("Passive Trait:\n" + monster.passive);
            passive.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            passive.setTextFill(Color.rgb(210, 225, 245));
            passive.setWrapText(true);

            Label powerup = new Label("Powerup:\n" + monster.powerup);
            powerup.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            powerup.setTextFill(Color.rgb(235, 220, 150));
            powerup.setWrapText(true);

            Label status = new Label(
                    "Live Status:\n" +
                            "Shield: " + (shield ? "Yes" : "None") +
                            "   |   Frozen: " + (frozen ? "Yes" : "No") +
                            "   |   Confusion: " + (confusion > 0 ? confusion + " turns" : "None")
            );
            status.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 13));
            status.setTextFill(Color.rgb(235, 240, 245));
            status.setWrapText(true);

            HBox buttons = new HBox(12);
            buttons.setAlignment(Pos.CENTER_LEFT);

            Button edit = UIEffects.createButton("EDIT NAME", roleColor, audioManager, 125, 38);
            Button close = UIEffects.createButton("CONTINUE", roleColor, audioManager, 135, 38);

            edit.setOnAction(e -> {
                root.getChildren().remove(overlay);
                editDisplayName(player);
            });

            close.setOnAction(e -> root.getChildren().remove(overlay));

            buttons.getChildren().addAll(edit, close);

            info.getChildren().addAll(headingLabel, name, controlled, meta, personality, passive, powerup, status, buttons);
            panel.getChildren().addAll(art, info);

            overlay.getChildren().add(panel);
            root.getChildren().add(overlay);

            panel.setOpacity(0);
            panel.setScaleX(0.86);
            panel.setScaleY(0.86);

            FadeTransition fade = new FadeTransition(Duration.millis(300), panel);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.millis(300), panel);
            scale.setToX(1);
            scale.setToY(1);
            scale.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(fade, scale).play();
        }

        private void showCardReveal(String cardName, String cardType, String cardEffect) {
            StackPane overlay = new StackPane();
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);
            overlay.setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 0, 0, 0.62),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            VBox card = new VBox(14);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(28));
            card.setPrefSize(430, 310);
            card.setMaxSize(430, 310);

            card.setBackground(new Background(new BackgroundFill(
                    Color.rgb(8, 12, 35, 0.96),
                    new CornerRadii(28),
                    Insets.EMPTY
            )));

            card.setBorder(new Border(new BorderStroke(
                    Color.rgb(215, 130, 235),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(28),
                    new BorderWidths(2.4)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(215, 130, 235, 0.70));
            glow.setRadius(32);
            glow.setSpread(0.30);
            card.setEffect(glow);

            Label top = new Label("CARD DRAWN");
            top.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 18));
            top.setTextFill(Color.rgb(220, 170, 240));

            Label name = new Label(cardName);
            name.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 34));
            name.setTextFill(Color.WHITE);
            name.setWrapText(true);
            name.setAlignment(Pos.CENTER);

            Label type = new Label(cardType);
            type.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            type.setTextFill(Color.rgb(125, 205, 235));

            Label effect = new Label(cardEffect);
            effect.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            effect.setTextFill(Color.rgb(225, 235, 242));
            effect.setWrapText(true);
            effect.setMaxWidth(340);
            effect.setAlignment(Pos.CENTER);

            Button close = UIEffects.createButton("CONTINUE", Color.rgb(215, 130, 235), audioManager, 150, 40);
            close.setOnAction(e -> root.getChildren().remove(overlay));

            card.getChildren().addAll(top, name, type, effect, close);

            overlay.getChildren().add(card);
            root.getChildren().add(overlay);

            card.setScaleX(0.45);
            card.setScaleY(0.45);
            card.setOpacity(0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(370), card);
            scale.setToX(1);
            scale.setToY(1);
            scale.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(370), card);
            fade.setToValue(1);

            RotateTransition rotate = new RotateTransition(Duration.millis(370), card);
            rotate.setFromAngle(-5);
            rotate.setToAngle(0);

            new ParallelTransition(scale, fade, rotate).play();
        }

        private void showEnergyPopup(String text, Color color) {
            Label popup = new Label(text);
            popup.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 27));
            popup.setTextFill(Color.WHITE);
            popup.setMouseTransparent(true);
            popup.setManaged(false);

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(color, 0.70));
            glow.setRadius(24);
            glow.setSpread(0.26);
            popup.setEffect(glow);

            popup.setLayoutX(APP_WIDTH / 2.0 - 80);
            popup.setLayoutY(APP_HEIGHT / 2.0 - 180);

            root.getChildren().add(popup);

            popup.setOpacity(0);
            popup.setTranslateY(28);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(110), popup);
            fadeIn.setToValue(1);

            TranslateTransition moveUp = new TranslateTransition(Duration.millis(800), popup);
            moveUp.setToY(-30);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(800), popup);
            fadeOut.setToValue(0);

            SequentialTransition sequence = new SequentialTransition(fadeIn, new ParallelTransition(moveUp, fadeOut));
            sequence.setOnFinished(e -> root.getChildren().remove(popup));
            sequence.play();
        }

        private void showWinScreen(MonsterProfile winner, int playerFinalEnergy, int opponentFinalEnergy) {
            boolean playerWon = winner == playerMonster;
            showVictoryCinematic(playerWon);
        }
        private void showVictoryCinematic(boolean playerWon) {
            gameOver = true;
            movementLocked = true;

            MonsterProfile winnerMonster = playerWon ? playerMonster : opponentMonster;
            MonsterProfile loserMonster = playerWon ? opponentMonster : playerMonster;

            String winnerName = playerWon ? playerDisplayName : opponentDisplayName;
            String loserName = playerWon ? opponentDisplayName : playerDisplayName;

            int winnerEnergy = playerWon ? playerEnergy : opponentEnergy;
            int loserEnergy = playerWon ? opponentEnergy : playerEnergy;

            boolean winnerIsScarer = "SCARER".equals(winnerMonster.role);
            Color winnerColor = winnerIsScarer ? Color.rgb(95, 145, 255) : Color.rgb(95, 255, 170);
            Color loserColor = winnerIsScarer ? Color.rgb(95, 255, 170) : Color.rgb(95, 145, 255);
            Color accentColor = winnerIsScarer ? Color.rgb(225, 65, 75) : Color.rgb(255, 215, 90);

            audioManager.stopMusic();

            String videoPath = winnerIsScarer
                    ? "/assets/videos/scarer_victory.mp4"
                    : "/assets/videos/laugher_victory.mp4";

            URL videoUrl = null;
            try {
                videoUrl = getClass().getResource(videoPath);
            } catch (Exception ex) {
                videoUrl = null;
            }

            if (videoUrl != null) {
                playVictoryVideoThenStats(videoUrl, winnerMonster, loserMonster, winnerName, loserName,
                        winnerEnergy, loserEnergy, winnerColor, accentColor, winnerIsScarer);
            } else {
                playVectorVictoryCinematic(winnerMonster, loserMonster, winnerName, loserName,
                        winnerEnergy, loserEnergy, winnerColor, loserColor, accentColor, winnerIsScarer);
            }
        }

        private void playVictoryVideoThenStats(URL videoUrl, MonsterProfile winnerMonster, MonsterProfile loserMonster,
                                               String winnerName, String loserName,
                                               int winnerEnergy, int loserEnergy,
                                               Color winnerColor, Color accentColor, boolean winnerIsScarer) {
            StackPane overlay = new StackPane();
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);
            overlay.setPickOnBounds(true);

            Rectangle blackBg = new Rectangle(APP_WIDTH, APP_HEIGHT);
            blackBg.setFill(Color.BLACK);

            MediaPlayer videoPlayer;
            MediaView videoView;
            try {
                Media media = new Media(videoUrl.toExternalForm());
                videoPlayer = new MediaPlayer(media);
                videoView = new MediaView(videoPlayer);
                videoView.setPreserveRatio(true);
                videoView.setFitWidth(APP_WIDTH);
                videoView.setFitHeight(APP_HEIGHT);
                videoView.setSmooth(true);
            } catch (Exception ex) {
                System.out.println("Could not initialize victory video: " + ex.getMessage());
                playVectorVictoryCinematic(winnerMonster, loserMonster, winnerName, loserName,
                        winnerEnergy, loserEnergy, winnerColor,
                        winnerIsScarer ? Color.rgb(95, 255, 170) : Color.rgb(95, 145, 255),
                        accentColor, winnerIsScarer);
                return;
            }

            Label skipHint = new Label("Click anywhere to skip \u2192");
            skipHint.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            skipHint.setTextFill(Color.rgb(220, 230, 240, 0.65));
            skipHint.setPadding(new Insets(6, 14, 6, 14));
            skipHint.setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 0, 0, 0.55),
                    new CornerRadii(12),
                    Insets.EMPTY
            )));
            StackPane.setAlignment(skipHint, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(skipHint, new Insets(0, 30, 30, 0));

            overlay.getChildren().addAll(blackBg, videoView, skipHint);
            root.getChildren().add(overlay);

            final MediaPlayer playerRef = videoPlayer;
            final boolean[] transitioned = {false};

            Runnable transitionToStats = () -> {
                if (transitioned[0]) return;
                transitioned[0] = true;
                try { playerRef.stop(); } catch (Exception ignore) {}

                FadeTransition fadeOut = new FadeTransition(Duration.millis(420), overlay);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    root.getChildren().remove(overlay);
                    showVictoryStatsPanel(winnerMonster, loserMonster, winnerName, loserName,
                            winnerEnergy, loserEnergy, winnerColor, accentColor, winnerIsScarer);
                });
                fadeOut.play();
            };

            videoPlayer.setOnEndOfMedia(transitionToStats);
            videoPlayer.setOnError(() -> {
                System.out.println("Victory video error: " + videoPlayer.getError());
                transitionToStats.run();
            });

            overlay.setOnMouseClicked(ev -> transitionToStats.run());

            try {
                videoPlayer.play();
            } catch (Exception ex) {
                System.out.println("Victory video failed to play: " + ex.getMessage());
                transitionToStats.run();
            }
        }

        private void showVictoryStatsPanel(MonsterProfile winnerMonster, MonsterProfile loserMonster,
                                           String winnerName, String loserName,
                                           int winnerEnergy, int loserEnergy,
                                           Color winnerColor, Color accentColor, boolean winnerIsScarer) {
            StackPane overlay = new StackPane();
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);
            overlay.setPickOnBounds(true);

            Rectangle dim = new Rectangle(APP_WIDTH, APP_HEIGHT);
            dim.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.85, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(winnerColor, 0.14)),
                    new Stop(0.55, Color.rgb(2, 5, 14, 0.94)),
                    new Stop(1, Color.rgb(0, 0, 0, 1))));

            Pane confettiLayer = new Pane();
            confettiLayer.setMouseTransparent(true);
            confettiLayer.setPrefSize(APP_WIDTH, APP_HEIGHT);

            StackPane card = new StackPane();
            card.setMaxSize(820, 540);
            card.setPrefSize(820, 540);

            Rectangle cardBg = new Rectangle(820, 540);
            cardBg.setArcWidth(34);
            cardBg.setArcHeight(34);
            cardBg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(10, 18, 38, 0.97)),
                    new Stop(1, Color.rgb(4, 8, 22, 0.97))));
            cardBg.setStroke(withAlpha(winnerColor, 0.92));
            cardBg.setStrokeWidth(2.4);

            DropShadow cardGlow = new DropShadow();
            cardGlow.setColor(withAlpha(winnerColor, 0.65));
            cardGlow.setRadius(38);
            cardGlow.setSpread(0.20);
            cardBg.setEffect(cardGlow);

            VBox content = new VBox(14);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(36, 40, 28, 40));

            HBox letters = new HBox(3);
            letters.setAlignment(Pos.CENTER);
            String victoryText = "VICTORY";
            java.util.List<Label> letterLabels = new java.util.ArrayList<>();
            for (int i = 0; i < victoryText.length(); i++) {
                Label l = new Label(String.valueOf(victoryText.charAt(i)));
                l.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 64));
                l.setTextFill(Color.WHITE);
                DropShadow lg = new DropShadow();
                lg.setColor(withAlpha(winnerColor, 0.98));
                lg.setRadius(24);
                lg.setSpread(0.34);
                l.setEffect(lg);
                l.setOpacity(0);
                l.setScaleX(0.3);
                l.setScaleY(0.3);
                letterLabels.add(l);
                letters.getChildren().add(l);
            }

            Label subtitle = new Label("BOO'S DOOR OPENED");
            subtitle.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 18));
            subtitle.setTextFill(withAlpha(winnerColor, 0.96));
            subtitle.setOpacity(0);

            HBox versus = new HBox(40);
            versus.setAlignment(Pos.CENTER);
            versus.setOpacity(0);

            Color loserColorLocal = winnerIsScarer ? Color.rgb(95, 255, 170) : Color.rgb(95, 145, 255);
            StackPane winnerArt = createCharacterCutout(winnerMonster, winnerColor, 200, 200, 175);
            StackPane loserArt = createCharacterCutout(loserMonster, loserColorLocal, 130, 130, 115);
            loserArt.setOpacity(0.45);
            loserArt.setRotate(winnerIsScarer ? 14 : -14);

            VBox centerInfo = new VBox(8);
            centerInfo.setAlignment(Pos.CENTER);
            centerInfo.setPrefWidth(280);

            Label winnerLine = new Label(winnerMonster.name);
            winnerLine.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 26));
            winnerLine.setTextFill(Color.rgb(248, 252, 255));

            Label roleLine = new Label("wins as " + winnerMonster.role);
            roleLine.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 16));
            roleLine.setTextFill(withAlpha(winnerColor, 0.95));

            Label userLine = new Label("Controlled by " + winnerName);
            userLine.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
            userLine.setTextFill(Color.rgb(190, 205, 220));

            Label detailText = new Label(
                    "Final Energy: 0\n" +
                            "Opponent Energy: 0\n" +
                            "Defeated: " + loserMonster.name + " (" + loserName + ")"
            );
            detailText.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            detailText.setTextFill(Color.rgb(220, 230, 240));
            detailText.setTextAlignment(TextAlignment.CENTER);

            centerInfo.getChildren().addAll(winnerLine, roleLine, userLine, detailText);
            versus.getChildren().addAll(winnerArt, centerInfo, loserArt);

            HBox buttons = new HBox(16);
            buttons.setAlignment(Pos.CENTER);
            buttons.setOpacity(0);

            Button playAgain = createVictoryButton("PLAY AGAIN", Color.rgb(85, 220, 255), () -> {
                audioManager.stopMusic();
                root.getChildren().clear();
                GameScreenView restartedGame = new GameScreenView(audioManager, config, onBackToStart);
                root.getChildren().add(restartedGame.getRoot());
            });

            Button backToStart = createVictoryButton("BACK TO START", winnerColor, () -> {
                audioManager.stopMusic();
                onBackToStart.run();
            });

            Button exitGame = createVictoryButton("EXIT GAME", Color.rgb(230, 105, 105), () -> {
                audioManager.stopAll();
                Platform.exit();
            });

            buttons.getChildren().addAll(playAgain, backToStart, exitGame);

            content.getChildren().addAll(letters, subtitle, versus, buttons);
            card.getChildren().addAll(cardBg, content);

            overlay.getChildren().addAll(dim, confettiLayer, card);

            root.getChildren().add(overlay);
            overlay.setOpacity(0);

            FadeTransition overlayIn = new FadeTransition(Duration.millis(380), overlay);
            overlayIn.setToValue(1);

            ScaleTransition cardIn = new ScaleTransition(Duration.millis(420), card);
            cardIn.setFromX(0.85);
            cardIn.setFromY(0.85);
            cardIn.setToX(1.0);
            cardIn.setToY(1.0);
            cardIn.setInterpolator(Interpolator.EASE_OUT);

            SequentialTransition lettersIn = new SequentialTransition();
            for (Label l : letterLabels) {
                FadeTransition lf = new FadeTransition(Duration.millis(90), l);
                lf.setToValue(1);
                ScaleTransition ls = new ScaleTransition(Duration.millis(140), l);
                ls.setToX(1.0);
                ls.setToY(1.0);
                ls.setInterpolator(Interpolator.EASE_OUT);
                lettersIn.getChildren().add(new ParallelTransition(lf, ls));
            }

            FadeTransition subtitleFade = new FadeTransition(Duration.millis(260), subtitle);
            subtitleFade.setToValue(1);

            FadeTransition versusFade = new FadeTransition(Duration.millis(320), versus);
            versusFade.setToValue(1);

            FadeTransition buttonsFade = new FadeTransition(Duration.millis(300), buttons);
            buttonsFade.setToValue(1);

            PauseTransition statsTrigger = new PauseTransition(Duration.millis(40));
            statsTrigger.setOnFinished(e -> animateVictoryStats(detailText, winnerEnergy, loserEnergy, loserMonster.name, loserName));

            PauseTransition musicTrigger = new PauseTransition(Duration.millis(40));
            musicTrigger.setOnFinished(e -> audioManager.playVictoryMusic());

            PauseTransition confettiTrigger = new PauseTransition(Duration.millis(40));
            confettiTrigger.setOnFinished(e -> {
                if (!winnerIsScarer) rainConfetti(confettiLayer, winnerColor, accentColor);
            });

            SequentialTransition full = new SequentialTransition(
                    new ParallelTransition(overlayIn, cardIn),
                    lettersIn,
                    subtitleFade,
                    versusFade,
                    statsTrigger,
                    confettiTrigger,
                    musicTrigger,
                    new PauseTransition(Duration.millis(200)),
                    buttonsFade
            );

            full.play();
        }

        private void playVectorVictoryCinematic(MonsterProfile winnerMonster, MonsterProfile loserMonster,
                                                String winnerName, String loserName,
                                                int winnerEnergy, int loserEnergy,
                                                Color winnerColor, Color loserColor, Color accentColor,
                                                boolean winnerIsScarer) {
            StackPane overlay = new StackPane();
            overlay.setPickOnBounds(true);
            overlay.setPrefSize(APP_WIDTH, APP_HEIGHT);

            Rectangle blackout = new Rectangle(APP_WIDTH, APP_HEIGHT);
            blackout.setFill(Color.BLACK);
            blackout.setOpacity(0);

            Rectangle stageBackdrop = new Rectangle(APP_WIDTH, APP_HEIGHT);
            stageBackdrop.setFill(new RadialGradient(0, 0, 0.5, 0.55, 0.70, true, CycleMethod.NO_CYCLE,
                    new Stop(0, withAlpha(winnerColor, 0.18)),
                    new Stop(0.55, Color.rgb(2, 6, 18, 0.97)),
                    new Stop(1, Color.rgb(0, 0, 0, 1))));
            stageBackdrop.setOpacity(0);

            Rectangle accentWash = new Rectangle(APP_WIDTH, APP_HEIGHT);
            accentWash.setFill(withAlpha(accentColor, 0.35));
            accentWash.setOpacity(0);
            accentWash.setMouseTransparent(true);

            Pane particleLayer = new Pane();
            particleLayer.setMouseTransparent(true);
            particleLayer.setPrefSize(APP_WIDTH, APP_HEIGHT);

            StackPane stageLayer = new StackPane();
            stageLayer.setPrefSize(APP_WIDTH, APP_HEIGHT);
            stageLayer.setMouseTransparent(true);

            StackPane winnerArt = createCharacterCutout(winnerMonster, winnerColor, 280, 320, 280);
            StackPane loserArt = createCharacterCutout(loserMonster, loserColor, 200, 220, 200);

            winnerArt.setManaged(false);
            loserArt.setManaged(false);

            double winnerStartX = winnerIsScarer ? -340 : APP_WIDTH + 80;
            double winnerCenterX = APP_WIDTH / 2.0 - 320;
            double loserCenterX = APP_WIDTH / 2.0 + 80;

            winnerArt.setLayoutX(winnerStartX);
            winnerArt.setLayoutY(APP_HEIGHT / 2.0 - 200);
            winnerArt.setOpacity(0);
            winnerArt.setScaleX(0.55);
            winnerArt.setScaleY(0.55);

            loserArt.setLayoutX(loserCenterX);
            loserArt.setLayoutY(-280);
            loserArt.setOpacity(0);

            stageLayer.getChildren().addAll(loserArt, winnerArt);

            HBox letters = new HBox(3);
            letters.setAlignment(Pos.CENTER);
            String victoryText = "VICTORY";
            java.util.List<Label> letterLabels = new java.util.ArrayList<>();
            for (int i = 0; i < victoryText.length(); i++) {
                Label l = new Label(String.valueOf(victoryText.charAt(i)));
                l.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 76));
                l.setTextFill(Color.WHITE);
                DropShadow lg = new DropShadow();
                lg.setColor(withAlpha(winnerColor, 0.98));
                lg.setRadius(26);
                lg.setSpread(0.35);
                l.setEffect(lg);
                l.setOpacity(0);
                l.setScaleX(0.3);
                l.setScaleY(0.3);
                letterLabels.add(l);
                letters.getChildren().add(l);
            }

            Label subtitle = new Label("BOO'S DOOR OPENED");
            subtitle.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 20));
            subtitle.setTextFill(withAlpha(winnerColor, 0.96));
            subtitle.setOpacity(0);

            Label winnerLine = new Label(winnerMonster.name + " wins as " + winnerMonster.role);
            winnerLine.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 22));
            winnerLine.setTextFill(Color.rgb(240, 245, 255));
            winnerLine.setOpacity(0);

            Label userLine = new Label("Controlled by: " + winnerName);
            userLine.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
            userLine.setTextFill(withAlpha(winnerColor, 0.95));
            userLine.setOpacity(0);

            Label detailText = new Label(
                    "Final Energy: 0\n" +
                            "Opponent Energy: 0\n" +
                            "Defeated: " + loserMonster.name + " (" + loserName + ")"
            );
            detailText.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            detailText.setTextFill(Color.rgb(220, 228, 240));
            detailText.setTextAlignment(TextAlignment.CENTER);
            detailText.setOpacity(0);

            VBox titleBox = new VBox(10);
            titleBox.setAlignment(Pos.CENTER);
            titleBox.setMouseTransparent(true);
            titleBox.getChildren().addAll(letters, subtitle, winnerLine, userLine, detailText);
            StackPane.setAlignment(titleBox, Pos.TOP_CENTER);
            StackPane.setMargin(titleBox, new Insets(80, 0, 0, 0));

            HBox buttons = new HBox(16);
            buttons.setAlignment(Pos.CENTER);
            buttons.setOpacity(0);

            Button playAgain = createVictoryButton("PLAY AGAIN", Color.rgb(85, 220, 255), () -> {
                audioManager.stopMusic();
                root.getChildren().clear();
                GameScreenView restartedGame = new GameScreenView(audioManager, config, onBackToStart);
                root.getChildren().add(restartedGame.getRoot());
            });

            Button backToStart = createVictoryButton("BACK TO START", winnerColor, () -> {
                audioManager.stopMusic();
                onBackToStart.run();
            });

            Button exitGame = createVictoryButton("EXIT GAME", Color.rgb(230, 105, 105), () -> {
                audioManager.stopAll();
                Platform.exit();
            });

            buttons.getChildren().addAll(playAgain, backToStart, exitGame);
            StackPane.setAlignment(buttons, Pos.BOTTOM_CENTER);
            StackPane.setMargin(buttons, new Insets(0, 0, 60, 0));

            overlay.getChildren().addAll(blackout, stageBackdrop, accentWash, particleLayer, stageLayer, titleBox, buttons);

            root.getChildren().add(overlay);
            overlay.setOpacity(0);

            FadeTransition overlayFade = new FadeTransition(Duration.millis(180), overlay);
            overlayFade.setToValue(1);

            FadeTransition blackoutIn = new FadeTransition(Duration.millis(320), blackout);
            blackoutIn.setToValue(1.0);

            PauseTransition holdBlack = new PauseTransition(Duration.millis(180));

            FadeTransition blackoutOut = new FadeTransition(Duration.millis(420), blackout);
            blackoutOut.setFromValue(1.0);
            blackoutOut.setToValue(0.50);

            FadeTransition backdropIn = new FadeTransition(Duration.millis(420), stageBackdrop);
            backdropIn.setToValue(1);

            ParallelTransition phase0 = new ParallelTransition(blackoutOut, backdropIn);

            FadeTransition loserFadeIn = new FadeTransition(Duration.millis(120), loserArt);
            loserFadeIn.setToValue(1);

            TranslateTransition loserDrop = new TranslateTransition(Duration.millis(360), loserArt);
            loserDrop.setFromY(0);
            loserDrop.setToY(APP_HEIGHT / 2.0 - 20);
            loserDrop.setInterpolator(Interpolator.EASE_IN);

            ParallelTransition loserSlam = new ParallelTransition(loserFadeIn, loserDrop);
            loserSlam.setOnFinished(e -> {
                audioManager.playEnergyLoss();
                triggerImpactShake(stageLayer);
                burstDust(particleLayer, loserCenterX + 100, APP_HEIGHT / 2.0 + 110, withAlpha(accentColor, 0.85));
            });

            PauseTransition postSlamPause = new PauseTransition(Duration.millis(260));

            ScaleTransition loserCrumple = new ScaleTransition(Duration.millis(360), loserArt);
            loserCrumple.setToX(0.78);
            loserCrumple.setToY(0.62);

            RotateTransition loserTilt = new RotateTransition(Duration.millis(360), loserArt);
            loserTilt.setToAngle(winnerIsScarer ? 22 : -22);

            FadeTransition loserDim = new FadeTransition(Duration.millis(900), loserArt);
            loserDim.setToValue(0.42);
            loserDim.setDelay(Duration.millis(500));

            ParallelTransition loserBroken = new ParallelTransition(loserCrumple, loserTilt);

            FadeTransition winnerFadeIn = new FadeTransition(Duration.millis(200), winnerArt);
            winnerFadeIn.setToValue(1);

            TranslateTransition winnerWalk = new TranslateTransition(Duration.millis(1000), winnerArt);
            winnerWalk.setFromX(0);
            winnerWalk.setToX(winnerCenterX - winnerStartX);
            winnerWalk.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition winnerGrow = new ScaleTransition(Duration.millis(1000), winnerArt);
            winnerGrow.setToX(1.0);
            winnerGrow.setToY(1.0);
            winnerGrow.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition winnerBob = new TranslateTransition(Duration.millis(180), winnerArt);
            winnerBob.setByY(-10);
            winnerBob.setAutoReverse(true);
            winnerBob.setCycleCount(6);

            ParallelTransition winnerEntry = new ParallelTransition(winnerFadeIn, winnerWalk, winnerGrow, winnerBob);

            ScaleTransition winnerPrePose = new ScaleTransition(Duration.millis(200), winnerArt);
            winnerPrePose.setToX(1.22);
            winnerPrePose.setToY(1.22);

            PauseTransition prePosePause = new PauseTransition(Duration.millis(120));

            PauseTransition signature = new PauseTransition(Duration.millis(20));
            signature.setOnFinished(ev -> {
                if (winnerIsScarer) {
                    audioManager.playScarerVictoryRoar();
                    triggerRoarShockwave(particleLayer, winnerCenterX + 140, APP_HEIGHT / 2.0, accentColor);
                    flashScreen(accentWash, 0.45, 240);
                    triggerImpactShake(stageLayer);
                } else {
                    audioManager.playLaugherVictoryLaugh();
                    burstLaughs(particleLayer, winnerCenterX + 140, APP_HEIGHT / 2.0 - 30, accentColor);
                    flashScreen(accentWash, 0.30, 220);
                    triggerWiggle(winnerArt);
                }
            });

            ScaleTransition winnerExpand = new ScaleTransition(Duration.millis(180), winnerArt);
            winnerExpand.setToX(1.42);
            winnerExpand.setToY(1.42);
            winnerExpand.setAutoReverse(true);
            winnerExpand.setCycleCount(2);

            PauseTransition postSignaturePause = new PauseTransition(Duration.millis(340));

            ScaleTransition winnerSettle = new ScaleTransition(Duration.millis(260), winnerArt);
            winnerSettle.setToX(1.10);
            winnerSettle.setToY(1.10);

            ParallelTransition signaturePhase = new ParallelTransition(winnerExpand, signature);

            SequentialTransition lettersIn = new SequentialTransition();
            for (Label l : letterLabels) {
                FadeTransition lf = new FadeTransition(Duration.millis(110), l);
                lf.setToValue(1);
                ScaleTransition ls = new ScaleTransition(Duration.millis(150), l);
                ls.setToX(1.0);
                ls.setToY(1.0);
                ls.setInterpolator(Interpolator.EASE_OUT);
                lettersIn.getChildren().add(new ParallelTransition(lf, ls));
            }

            FadeTransition subtitleFade = new FadeTransition(Duration.millis(260), subtitle);
            subtitleFade.setToValue(1);
            FadeTransition winnerLineFade = new FadeTransition(Duration.millis(260), winnerLine);
            winnerLineFade.setToValue(1);
            FadeTransition userLineFade = new FadeTransition(Duration.millis(260), userLine);
            userLineFade.setToValue(1);
            FadeTransition detailFade = new FadeTransition(Duration.millis(260), detailText);
            detailFade.setToValue(1);

            ParallelTransition textBlock = new ParallelTransition(
                    subtitleFade,
                    new SequentialTransition(new PauseTransition(Duration.millis(120)), winnerLineFade),
                    new SequentialTransition(new PauseTransition(Duration.millis(240)), userLineFade),
                    new SequentialTransition(new PauseTransition(Duration.millis(360)), detailFade)
            );

            FadeTransition buttonsFade = new FadeTransition(Duration.millis(320), buttons);
            buttonsFade.setToValue(1);

            PauseTransition statsTrigger = new PauseTransition(Duration.millis(40));
            statsTrigger.setOnFinished(e -> animateVictoryStats(detailText, winnerEnergy, loserEnergy, loserMonster.name, loserName));

            PauseTransition musicTrigger = new PauseTransition(Duration.millis(40));
            musicTrigger.setOnFinished(e -> {
                audioManager.playVictory();
                audioManager.playVictoryMusic();
            });

            PauseTransition confettiTrigger = new PauseTransition(Duration.millis(40));
            confettiTrigger.setOnFinished(ev -> {
                if (!winnerIsScarer) rainConfetti(particleLayer, winnerColor, accentColor);
            });

            SequentialTransition full = new SequentialTransition(
                    overlayFade,
                    blackoutIn,
                    holdBlack,
                    phase0,
                    confettiTrigger,
                    loserSlam,
                    postSlamPause,
                    loserBroken,
                    winnerEntry,
                    prePosePause,
                    winnerPrePose,
                    signaturePhase,
                    postSignaturePause,
                    winnerSettle,
                    lettersIn,
                    textBlock,
                    statsTrigger,
                    musicTrigger,
                    buttonsFade
            );

            loserDim.play();
            full.play();
        }


        private void triggerImpactShake(javafx.scene.Node target) {
            TranslateTransition t = new TranslateTransition(Duration.millis(50), target);
            t.setByX(8);
            t.setAutoReverse(true);
            t.setCycleCount(6);
            t.setOnFinished(e -> target.setTranslateX(0));
            t.play();
        }

        private void triggerWiggle(javafx.scene.Node target) {
            RotateTransition r = new RotateTransition(Duration.millis(80), target);
            r.setByAngle(8);
            r.setAutoReverse(true);
            r.setCycleCount(6);
            r.setOnFinished(e -> target.setRotate(0));
            r.play();
        }

        private void flashScreen(Rectangle accentWash, double peakOpacity, int durationMs) {
            FadeTransition up = new FadeTransition(Duration.millis(durationMs / 2), accentWash);
            up.setFromValue(0);
            up.setToValue(peakOpacity);
            FadeTransition down = new FadeTransition(Duration.millis(durationMs / 2), accentWash);
            down.setFromValue(peakOpacity);
            down.setToValue(0);
            new SequentialTransition(up, down).play();
        }

        private void triggerRoarShockwave(Pane layer, double cx, double cy, Color color) {
            for (int i = 0; i < 3; i++) {
                Circle wave = new Circle(40);
                wave.setFill(Color.TRANSPARENT);
                wave.setStroke(withAlpha(color, 0.95));
                wave.setStrokeWidth(5);
                wave.setManaged(false);
                wave.setLayoutX(cx);
                wave.setLayoutY(cy);
                DropShadow wg = new DropShadow();
                wg.setColor(withAlpha(color, 0.90));
                wg.setRadius(24);
                wg.setSpread(0.30);
                wave.setEffect(wg);
                layer.getChildren().add(wave);

                ScaleTransition expand = new ScaleTransition(Duration.millis(700), wave);
                expand.setFromX(0.4);
                expand.setFromY(0.4);
                expand.setToX(14.0);
                expand.setToY(14.0);
                expand.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition fade = new FadeTransition(Duration.millis(700), wave);
                fade.setFromValue(0.95);
                fade.setToValue(0);
                ParallelTransition p = new ParallelTransition(expand, fade);
                p.setDelay(Duration.millis(i * 140));
                p.setOnFinished(e -> layer.getChildren().remove(wave));
                p.play();
            }

            Random random = new Random();
            for (int i = 0; i < 28; i++) {
                Circle spark = new Circle(2 + random.nextDouble() * 4);
                spark.setFill(withAlpha(color, 0.95));
                spark.setManaged(false);
                spark.setLayoutX(cx);
                spark.setLayoutY(cy);
                DropShadow sg = new DropShadow();
                sg.setColor(withAlpha(color, 0.85));
                sg.setRadius(12);
                sg.setSpread(0.20);
                spark.setEffect(sg);
                layer.getChildren().add(spark);

                double angle = random.nextDouble() * 2 * Math.PI;
                double dist = 150 + random.nextDouble() * 220;
                double dx = Math.cos(angle) * dist;
                double dy = Math.sin(angle) * dist;

                TranslateTransition fly = new TranslateTransition(Duration.millis(600 + random.nextInt(300)), spark);
                fly.setToX(dx);
                fly.setToY(dy);
                fly.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition fadeS = new FadeTransition(Duration.millis(600 + random.nextInt(300)), spark);
                fadeS.setFromValue(1);
                fadeS.setToValue(0);
                ParallelTransition pt = new ParallelTransition(fly, fadeS);
                pt.setOnFinished(e -> layer.getChildren().remove(spark));
                pt.play();
            }
        }

        private void burstDust(Pane layer, double cx, double cy, Color color) {
            Random random = new Random();
            for (int i = 0; i < 18; i++) {
                Circle d = new Circle(4 + random.nextDouble() * 5);
                d.setFill(withAlpha(color, 0.65));
                d.setManaged(false);
                d.setLayoutX(cx);
                d.setLayoutY(cy);
                layer.getChildren().add(d);

                double dx = (random.nextDouble() * 240 - 120);
                double dy = -(20 + random.nextDouble() * 60);

                TranslateTransition tt = new TranslateTransition(Duration.millis(500 + random.nextInt(250)), d);
                tt.setToX(dx);
                tt.setToY(dy);
                FadeTransition ft = new FadeTransition(Duration.millis(500 + random.nextInt(250)), d);
                ft.setFromValue(0.85);
                ft.setToValue(0);
                ParallelTransition p = new ParallelTransition(tt, ft);
                p.setOnFinished(e -> layer.getChildren().remove(d));
                p.play();
            }
        }

        private void burstLaughs(Pane layer, double cx, double cy, Color color) {
            Random random = new Random();
            String[] phrases = {"HA!", "HA HA!", "HEH", "HA!", "HA HA HA"};
            for (int i = 0; i < 14; i++) {
                Label ha = new Label(phrases[random.nextInt(phrases.length)]);
                ha.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 18 + random.nextInt(16)));
                ha.setTextFill(withAlpha(color, 0.95));
                DropShadow g = new DropShadow();
                g.setColor(withAlpha(color, 0.85));
                g.setRadius(14);
                g.setSpread(0.32);
                ha.setEffect(g);
                ha.setManaged(false);
                ha.setLayoutX(cx);
                ha.setLayoutY(cy);
                layer.getChildren().add(ha);

                double angle = random.nextDouble() * 2 * Math.PI;
                double dist = 140 + random.nextDouble() * 200;
                double dx = Math.cos(angle) * dist;
                double dy = Math.sin(angle) * dist;

                TranslateTransition tt = new TranslateTransition(Duration.millis(900 + random.nextInt(400)), ha);
                tt.setToX(dx);
                tt.setToY(dy);
                tt.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition ft = new FadeTransition(Duration.millis(900 + random.nextInt(400)), ha);
                ft.setFromValue(1.0);
                ft.setToValue(0);
                RotateTransition rt = new RotateTransition(Duration.millis(900 + random.nextInt(400)), ha);
                rt.setByAngle(random.nextDouble() * 60 - 30);
                ParallelTransition p = new ParallelTransition(tt, ft, rt);
                p.setOnFinished(e -> layer.getChildren().remove(ha));
                p.play();
            }
        }

        private void rainConfetti(Pane layer, Color baseColor, Color accentColor) {
            Random random = new Random();
            for (int i = 0; i < 80; i++) {
                Rectangle conf = new Rectangle(6 + random.nextDouble() * 4, 10 + random.nextDouble() * 6);
                int pick = random.nextInt(3);
                if (pick == 0) conf.setFill(withAlpha(baseColor, 0.95));
                else if (pick == 1) conf.setFill(withAlpha(accentColor, 0.95));
                else conf.setFill(Color.rgb(255, 255, 255, 0.85));
                conf.setManaged(false);
                conf.setLayoutX(random.nextDouble() * APP_WIDTH);
                conf.setLayoutY(-30);
                conf.setRotate(random.nextDouble() * 360);
                layer.getChildren().add(conf);

                double duration = 2200 + random.nextDouble() * 1800;
                TranslateTransition fall = new TranslateTransition(Duration.millis(duration), conf);
                fall.setToY(APP_HEIGHT + 60);
                fall.setByX(random.nextDouble() * 80 - 40);
                fall.setInterpolator(Interpolator.LINEAR);
                RotateTransition spin = new RotateTransition(Duration.millis(duration), conf);
                spin.setByAngle(720 + random.nextInt(360));
                ParallelTransition p = new ParallelTransition(fall, spin);
                p.setDelay(Duration.millis(random.nextInt(2000)));
                p.setOnFinished(e -> layer.getChildren().remove(conf));
                p.play();
            }
        }
        private Button createVictoryButton(String text, Color glowColor, Runnable action) {
            Button button = new Button(text);
            button.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 15));
            button.setTextFill(Color.WHITE);
            button.setPrefSize(195, 50);
            button.setFocusTraversable(false);

            button.setBackground(new Background(new BackgroundFill(
                    new LinearGradient(
                            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0.00, Color.rgb(14, 24, 48)),
                            new Stop(1.00, Color.rgb(28, 40, 72))
                    ),
                    new CornerRadii(18),
                    Insets.EMPTY
            )));

            button.setBorder(new Border(new BorderStroke(
                    withAlpha(glowColor, 0.95),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(18),
                    new BorderWidths(1.8)
            )));

            DropShadow glow = new DropShadow();
            glow.setColor(withAlpha(glowColor, 0.62));
            glow.setRadius(20);
            glow.setSpread(0.18);
            button.setEffect(glow);

            button.setOnMouseEntered(e -> {
                audioManager.playButtonHover();

                ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);
                scale.setToX(1.04);
                scale.setToY(1.04);
                scale.play();

                glow.setRadius(28);
                glow.setSpread(0.25);
            });

            button.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                glow.setRadius(20);
                glow.setSpread(0.18);
            });

            button.setOnAction(e -> {
                audioManager.playButtonClick();

                ScaleTransition click = new ScaleTransition(Duration.millis(90), button);
                click.setFromX(1.0);
                click.setFromY(1.0);
                click.setToX(0.96);
                click.setToY(0.96);
                click.setAutoReverse(true);
                click.setCycleCount(2);
                click.setOnFinished(ev -> action.run());
                click.play();
            });

            return button;
        }private void launchVictoryParticles(Pane layer, Color baseColor) {
            Random random = new Random();

            for (int i = 0; i < 42; i++){
            	Circle particle = new Circle(2.5 + random.nextDouble() * 5);

            	if (i % 3 == 0) {
            	    particle.setFill(Color.rgb(255, 215, 90, 0.95));
            	} else if (i % 3 == 1) {
            	    particle.setFill(withAlpha(baseColor, 0.95));
            	} else {
            	    particle.setFill(Color.rgb(255, 255, 255, 0.85));
            	}
                particle.setManaged(false);

                double startX = 465 + (random.nextDouble() * 120 - 60);
                double startY = 220 + (random.nextDouble() * 40 - 20);

                particle.setLayoutX(startX);
                particle.setLayoutY(startY);
                particle.setOpacity(0);

                DropShadow glow = new DropShadow();
                glow.setColor(withAlpha(baseColor, 0.85));
                glow.setRadius(14);
                glow.setSpread(0.20);
                particle.setEffect(glow);

                layer.getChildren().add(particle);

                double endX = startX + (random.nextDouble() * 280 - 140);
                double endY = startY - (90 + random.nextDouble() * 140);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(120), particle);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition fly = new TranslateTransition(Duration.millis(900 + random.nextInt(400)), particle);
                fly.setFromX(0);
                fly.setFromY(0);
                fly.setToX(endX - startX);
                fly.setToY(endY - startY);
                fly.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(900 + random.nextInt(400)), particle);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);

                ScaleTransition shrink = new ScaleTransition(Duration.millis(900 + random.nextInt(400)), particle);
                shrink.setFromX(1.0);
                shrink.setFromY(1.0);
                shrink.setToX(0.2);
                shrink.setToY(0.2);

                ParallelTransition burst = new ParallelTransition(fly, fadeOut, shrink);

                SequentialTransition full = new SequentialTransition(
                        new PauseTransition(Duration.millis(random.nextInt(220))),
                        fadeIn,
                        burst
                );

                full.setOnFinished(e -> layer.getChildren().remove(particle));
                full.play();
            }
        }
        private void animateVictoryStats(Label detailText, int winnerEnergy, int loserEnergy, String loserMonsterName, String loserName) {
            final int frames = 40;
            final int[] currentFrame = {0};

            Timeline counter = new Timeline(new KeyFrame(Duration.millis(25), e -> {
                currentFrame[0]++;

                double progress = currentFrame[0] / (double) frames;
                progress = Math.min(1.0, progress);

                int shownWinnerEnergy = (int) Math.round(winnerEnergy * progress);
                int shownLoserEnergy = (int) Math.round(loserEnergy * progress);

                detailText.setText(
                        "Final Energy: " + shownWinnerEnergy + "\n" +
                                "Opponent Energy: " + shownLoserEnergy + "\n" +
                                "Defeated: " + loserMonsterName + " (" + loserName + ")"
                );
            }));

            counter.setCycleCount(frames);
            counter.play();
        }

        private void log(String message) {
            if (eventLog == null) return;
            eventLog.appendText(message + "\n");
        }
    }
}