export type Mood = 'GREAT' | 'GOOD' | 'OKAY' | 'SAD' | 'STRESSED';

export interface WellnessCheckinRequest {
  classId: string;
  mood: Mood;
  note?: string;
  anonymous: boolean;
}

export interface ClassPulseDto {
  classId: string;
  className: string;
  date: string;
  totalCheckins: number;
  moodBreakdown: Record<string, number>;
  positiveCount: number;
  neutralCount: number;
  negativeCount: number;
  negativePct: number;
  alertTriggered: boolean;
}

export const MOOD_CONFIG: Record<Mood, { label: string; emoji: string; color: string }> = {
  GREAT:    { label: 'Great',    emoji: '😄', color: '#22c55e' },
  GOOD:     { label: 'Good',     emoji: '🙂', color: '#84cc16' },
  OKAY:     { label: 'Okay',     emoji: '😐', color: '#eab308' },
  SAD:      { label: 'Sad',      emoji: '😢', color: '#f97316' },
  STRESSED: { label: 'Stressed', emoji: '😰', color: '#ef4444' },
};
