#include <pebble.h>
#include <string.h>

#define KEY_BUTTON_UP   0
#define KEY_BUTTON_DOWN 1
#define KEY_BUTTON_SELECT 2

static Window *s_main_window;
static TextLayer *s_output_layer;
static char* default_message = 
"UP: save place \n\n SELECT: help \n\n DOWN: see saved places";


static GBitmap *s_bitmap;
static BitmapLayer *s_bitmap_layer;


static const uint32_t FOX_ICON[] = {
  RESOURCE_ID_FOX_NONE,
  RESOURCE_ID_FOX_SAVE,
  RESOURCE_ID_FOX_HELP,
  RESOURCE_ID_FOX_LIST
};

static void send(int key, int value) {
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);

  dict_write_int(iter, key, &value, sizeof(int), true);

  app_message_outbox_send();
}

static void outbox_sent_handler(DictionaryIterator *iter, void *context) {
  // Ready for next command
  psleep(1500);
  text_layer_set_text(s_output_layer, default_message);
  text_layer_set_text_color(s_output_layer, GColorWhite);
  text_layer_set_background_color(s_output_layer, GColorRed);
  window_set_background_color(s_main_window, GColorRed);
  
  gbitmap_destroy(s_bitmap);
  s_bitmap = gbitmap_create_with_resource(FOX_ICON[0]);
}

static void outbox_failed_handler(DictionaryIterator *iter, AppMessageResult reason, void *context) {
  text_layer_set_text(s_output_layer, "Send failed! Press any button to try reconnecting.");
  APP_LOG(APP_LOG_LEVEL_ERROR, "Fail reason: %d", (int)reason);
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  send(KEY_BUTTON_UP, 0);
  text_layer_set_text(s_output_layer, "Place saved!");
  text_layer_set_text_color(s_output_layer, GColorWhite);
  text_layer_set_background_color(s_output_layer, GColorRed);
  window_set_background_color(s_main_window, GColorRed);
  text_layer_set_text_alignment(s_output_layer, GTextAlignmentCenter);
  
  s_bitmap = gbitmap_create_with_resource(RESOURCE_ID_FOX_SAVE);
  bitmap_layer_set_compositing_mode(s_bitmap_layer, GCompOpAssign);
  bitmap_layer_set_bitmap(s_bitmap_layer, s_bitmap);
  bitmap_layer_set_alignment(s_bitmap_layer, GAlignCenter);
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  send(KEY_BUTTON_SELECT, 0);
  text_layer_set_text(s_output_layer, "Help is on its way!");
  text_layer_set_text_color(s_output_layer, GColorWhite);
  text_layer_set_background_color(s_output_layer, GColorRed);
  window_set_background_color(s_main_window, GColorRed);
  text_layer_set_text_alignment(s_output_layer, GTextAlignmentCenter);
  
  s_bitmap = gbitmap_create_with_resource(RESOURCE_ID_FOX_HELP);
  bitmap_layer_set_compositing_mode(s_bitmap_layer, GCompOpAssign);
  bitmap_layer_set_bitmap(s_bitmap_layer, s_bitmap);
  bitmap_layer_set_alignment(s_bitmap_layer, GAlignCenter);
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  send(KEY_BUTTON_DOWN, 0);
  text_layer_set_text(s_output_layer, "See saved places on phone!");
  text_layer_set_text_color(s_output_layer, GColorWhite);
  text_layer_set_background_color(s_output_layer, GColorRed);
  window_set_background_color(s_main_window, GColorRed);
  text_layer_set_text_alignment(s_output_layer, GTextAlignmentCenter);
  
  s_bitmap = gbitmap_create_with_resource(RESOURCE_ID_FOX_LIST);
  bitmap_layer_set_compositing_mode(s_bitmap_layer, GCompOpAssign);
  bitmap_layer_set_bitmap(s_bitmap_layer, s_bitmap);
  bitmap_layer_set_alignment(s_bitmap_layer, GAlignCenter);
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}

static void main_window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  const int text_height = 21;
  const GEdgeInsets text_insets = GEdgeInsets((bounds.size.h - 6 * text_height) / 2, 0);

  window_set_background_color(s_main_window, GColorRed);
  
  s_output_layer = text_layer_create(grect_inset(bounds, text_insets));
  text_layer_set_text(s_output_layer, default_message);
  text_layer_set_text_alignment(s_output_layer, GTextAlignmentCenter);
  text_layer_set_text_color(s_output_layer, GColorWhite);
  text_layer_set_background_color(s_output_layer, GColorRed);
  text_layer_set_font(s_output_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  layer_add_child(window_layer, text_layer_get_layer(s_output_layer));
  
  s_bitmap = gbitmap_create_with_resource(FOX_ICON[0]);
  s_bitmap_layer = bitmap_layer_create(bounds);
  layer_add_child(window_get_root_layer(window), bitmap_layer_get_layer(s_bitmap_layer));
}

static void main_window_unload(Window *window) {
  text_layer_destroy(s_output_layer);
  gbitmap_destroy(s_bitmap);
  bitmap_layer_destroy(s_bitmap_layer);
}

static void init(void) {
  s_main_window = window_create();
  window_set_click_config_provider(s_main_window, click_config_provider);
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload,
  });
  window_stack_push(s_main_window, true);

  // Open AppMessage
  app_message_register_outbox_sent(outbox_sent_handler);
  app_message_register_outbox_failed(outbox_failed_handler);
  
  const int inbox_size = 128;
  const int outbox_size = 128;
  app_message_open(inbox_size, outbox_size);
}

static void deinit(void) {
  window_destroy(s_main_window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}
