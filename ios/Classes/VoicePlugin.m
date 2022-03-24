#import "VoicePlugin.h"
#if __has_include(<voice/voice-Swift.h>)
#import <voice/voice-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "voice-Swift.h"
#endif

@implementation VoicePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftVoicePlugin registerWithRegistrar:registrar];
}
@end
