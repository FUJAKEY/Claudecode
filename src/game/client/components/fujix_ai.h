#ifndef GAME_CLIENT_COMPONENTS_FUJIX_AI_H
#define GAME_CLIENT_COMPONENTS_FUJIX_AI_H

#include <game/client/component.h>
#include <vector>
#include <base/vmath.h>

struct AStarNode
{
	int X, Y;
	int G, H;
	AStarNode *pParent;

	int F() const { return G + H; }
	bool operator==(const AStarNode &Other) const { return X == Other.X && Y == Other.Y; }
};

class CFujixAI : public CComponent
{
public:
	int Sizeof() const override { return sizeof(*this); }
	void OnRender() override;
	void OnInit() override;

private:
	void UpdateControls();
	
	// Pathfinding
	std::vector<vec2> m_Path;
	int m_PathIndex;
	int m_LastPathFindTime;
	
	void FindPath(vec2 Start, vec2 End);
	bool IsSolid(int x, int y) const;

	// Prediction
	CCharacterCore m_PredictCore;
	CWorldCore m_PredictWorld;
	
	// Helper to predict N ticks ahead
	vec2 PredictPos(vec2 Pos, vec2 Vel, int Inputs, int Ticks);
	
	// Advanced Hook
	void UpdateHook(CControls *pControls);
};

#endif
